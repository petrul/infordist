"""Serves per-word embeddings (rows of the PCA-reduced matrix written by

main.py's save_embeddings) over HTTP: a REST API plus a plain HTML page
listing every known term, click one to see its vector.

components.npy is opened once, memmapped read-only -- serving a term's
embedding only pages in that one row's bytes from disk, the whole file
is never loaded into RAM regardless of how many terms/dimensions it has.

Run with: uv run infordist-pca-server <pca-dir>
      or: uv run uvicorn infordist_pca.server:app --host 0.0.0.0 --port 8001
          (set INFORDIST_PCA_DIR env var first in that form)
"""
import html
import os
import sys
from pathlib import Path

import numpy as np
from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse

from .main import load_embeddings


def _resolve_pca_dir() -> Path:
  # INFORDIST_PCA_DIR takes priority: when uvicorn imports this module
  # directly (`uvicorn infordist_pca.server:app`, needed for --reload),
  # sys.argv is uvicorn's *own* argv (sys.argv[1] ends up being the
  # "infordist_pca.server:app" app-spec string, not a directory), so
  # sys.argv is only a meaningful source when launched via our own
  # `infordist-pca-server <dir>` entry point instead.
  env = os.environ.get("INFORDIST_PCA_DIR")
  arg = sys.argv[1] if len(sys.argv) > 1 else None
  raw = env or arg
  if not raw:
    raise SystemExit(
        "Usage: infordist-pca-server <pca-dir>  (a directory written by "
        "`infordist-pca`, containing components.npy/eigenvalues.npy/terms.json), "
        "or set INFORDIST_PCA_DIR and run uvicorn directly."
    )
  path = Path(raw)
  if not (path / "components.npy").exists():
    raise SystemExit(f"'{path}' doesn't look like a pca output dir (no components.npy)")
  return path


PCA_DIR = _resolve_pca_dir()
COMPONENTS, EIGENVALUES, TERM_INDEX, FREQUENCIES = load_embeddings(PCA_DIR)

if TERM_INDEX is None:
  raise SystemExit(
      f"'{PCA_DIR}' has no terms.json -- it was produced without term labels "
      "(terms.mat couldn't be parsed for that run), so there's nothing to "
      "look embeddings up by name for."
  )

TERMS_BY_ROW = [None] * len(TERM_INDEX)
for term, row in TERM_INDEX.items():
  TERMS_BY_ROW[row] = term

# Built once at startup rather than per-request: the sidebar wants every
# term (+ frequency, for the frequency sort) up front so it can sort/filter
# entirely client-side with no round-trip per keystroke. FREQUENCIES is
# None if complexities.binarymatrix wasn't found when this pca dir was
# generated (an older run, before that file was read) -- 0 for every term
# in that case, which just makes the frequency sort a no-op rather than
# an error.
ALL_TERMS_WITH_FREQ = [
    {"term": t, "freq": int(FREQUENCIES[i]) if FREQUENCIES is not None else 0}
    for i, t in enumerate(TERMS_BY_ROW)
]

# Precomputed once at startup, not lazily per-request: finding nearest
# neighbors for *any* term needs to compare against *every* row, so
# there's no lazy-paging benefit left to preserve here the way there is
# for a single-row /embedding lookup -- normalizing every row now means
# each /neighbors query is just one matrix-vector product instead of
# redoing the norm computation from scratch every time. This does mean
# the whole (n, k) matrix ends up resident in RAM as a plain float32
# array (e.g. ~200MB for 55000 terms x 1000 dims) -- small next to the
# original n x n co-occurrence matrix this was derived from.
_all_vectors = np.asarray(COMPONENTS, dtype=np.float32)
_norms = np.linalg.norm(_all_vectors, axis=1, keepdims=True)
NORMALIZED_COMPONENTS = _all_vectors / np.clip(_norms, 1e-12, None)

app = FastAPI(
    title="Infordist term embeddings",
    description="Serves rows of a PCA-reduced term co-occurrence matrix, by term.",
)


@app.get("/terms")
def list_terms() -> list[str]:
  return TERMS_BY_ROW


@app.get("/terms/full")
def list_terms_full() -> list[dict]:
  """Every term with its absolute frequency, in row order -- fetched

  once by the UI on page load so the sidebar can sort/filter locally
  instead of round-tripping to the server on every keystroke.
  """
  return ALL_TERMS_WITH_FREQ


@app.get("/embedding/{term}")
def get_embedding(term: str) -> dict:
  row = TERM_INDEX.get(term)
  if row is None:
    raise HTTPException(status_code=404, detail=f"no such term: '{term}'")
  vector = COMPONENTS[row]  # pages in just this one row from disk
  return {"term": term, "row": row, "dim": vector.shape[0], "vector": vector.tolist()}


@app.get("/embeddings")
def get_embeddings_batch(terms: str) -> list[dict]:
  """?terms=a,b,c -- batch lookup, one entry per term (missing terms

  come back with vector: null rather than failing the whole batch).
  """
  results = []
  for term in terms.split(","):
    term = term.strip()
    row = TERM_INDEX.get(term)
    if row is None:
      results.append({"term": term, "row": None, "vector": None})
    else:
      results.append({"term": term, "row": row, "vector": COMPONENTS[row].tolist()})
  return results


@app.get("/neighbors/{term}")
def get_neighbors(term: str, k: int = 10) -> dict:
  """Cosine-similarity nearest neighbors of `term` among every other

  known term -- an exact brute-force search (one matrix-vector product
  against the precomputed normalized embeddings), not an approximate
  index; at tens of thousands of terms x ~1000 dims this is still well
  under a second per query, so an ANN index isn't warranted here.
  """
  row = TERM_INDEX.get(term)
  if row is None:
    raise HTTPException(status_code=404, detail=f"no such term: '{term}'")
  k = max(1, min(k, len(TERMS_BY_ROW) - 1))

  sims = NORMALIZED_COMPONENTS @ NORMALIZED_COMPONENTS[row]
  sims[row] = -np.inf  # never return the term as its own neighbor
  top_idx = np.argpartition(-sims, k)[:k]
  top_idx = top_idx[np.argsort(-sims[top_idx])]

  return {
      "term": term,
      "neighbors": [
          {"term": TERMS_BY_ROW[i], "similarity": round(float(sims[i]), 4)}
          for i in top_idx
      ],
  }


@app.get("/", response_class=HTMLResponse)
def index() -> str:
  n, k = COMPONENTS.shape
  return f"""
  <!doctype html><html><head><title>Infordist term embeddings</title>
    <style>
      html, body {{ height: 100%; margin: 0; font-family: sans-serif; }}
      body {{ display: flex; flex-direction: column; }}
      header {{ padding: 0.8em 1em; border-bottom: 1px solid #ddd; flex: none; }}
      header input {{ font-size: 1.1em; padding: 0.4em; width: 320px; }}
      header select {{ font-size: 1em; padding: 0.4em; margin-left: 0.5em; }}
      #layout {{ flex: 1; display: flex; min-height: 0; }}
      #sidebar {{ width: 280px; flex: none; overflow-y: auto; border-right: 1px solid #ddd; }}
      #sidebar ul {{ list-style: none; margin: 0; padding: 0; }}
      #sidebar li {{ padding: 0.4em 0.8em; cursor: pointer; border-bottom: 1px solid #f0f0f0;
                     display: flex; justify-content: space-between; font-size: 0.95em; }}
      #sidebar li:hover, #sidebar li.selected {{ background: #eef4ff; }}
      #sidebar .freq {{ color: #999; font-size: 0.85em; }}
      #main {{ flex: 1; padding: 1em 1.5em; overflow-y: auto; }}
      #vector {{ font-family: monospace; white-space: pre-wrap; word-break: break-all;
                 background: #f4f4f4; padding: 1em; border-radius: 4px; max-height: 200px; overflow-y: auto; }}
      #neighbors {{ list-style: none; padding: 0; max-width: 500px; }}
      #neighbors li {{ display: flex; justify-content: space-between; padding: 0.3em 0.6em;
                        border-bottom: 1px solid #eee; }}
      #neighbors li:hover {{ background: #f4f4f4; cursor: pointer; }}
      #neighbors .sim {{ color: #888; font-family: monospace; }}
    </style>
  </head><body>
    <header>
      <strong>Infordist term embeddings</strong> &middot; {n} terms &middot; {k} dims
      &middot; source: <code>{html.escape(str(PCA_DIR))}</code>
      <div style="margin-top: 0.5em;">
        <input id="search-box" placeholder="search terms..." autocomplete="off">
        <select id="sort-mode">
          <option value="alpha">sort: alphabetical</option>
          <option value="freq">sort: frequency</option>
        </select>
      </div>
    </header>
    <div id="layout">
      <div id="sidebar"><ul id="term-list"></ul></div>
      <div id="main">
        <p id="status">Pick a term from the list on the left.</p>
        <h3>Vector</h3>
        <div id="vector"></div>
        <h3>Closest neighbors</h3>
        <ul id="neighbors"></ul>
      </div>
    </div>
    <script>
      let ALL_TERMS = [];
      let selectedTerm = null;

      function renderSidebar() {{
        const query = document.getElementById('search-box').value.trim().toLowerCase();
        const sortMode = document.getElementById('sort-mode').value;

        let items = query
          ? ALL_TERMS.filter(t => t.term.toLowerCase().includes(query))
          : ALL_TERMS;

        items = items.slice(); // don't mutate ALL_TERMS's own order
        if (sortMode === 'freq') {{
          items.sort((a, b) => b.freq - a.freq);
        }} else {{
          items.sort((a, b) => a.term.localeCompare(b.term));
        }}

        // Cap DOM rendering regardless of match count, so a broad/empty
        // search on a 50k+ term list stays responsive.
        const capped = items.slice(0, 500);
        const list = document.getElementById('term-list');
        list.innerHTML = capped.map(t =>
          `<li data-term="${{encodeURIComponent(t.term)}}" class="${{t.term === selectedTerm ? 'selected' : ''}}">` +
          `<span>${{t.term}}</span><span class="freq">${{t.freq}}</span></li>`
        ).join('');
        if (items.length > capped.length) {{
          const more = document.createElement('li');
          more.style.color = '#999';
          more.style.fontStyle = 'italic';
          more.textContent = `...and ${{items.length - capped.length}} more (refine your search)`;
          list.appendChild(more);
        }}
      }}

      document.getElementById('term-list').addEventListener('click', (e) => {{
        const li = e.target.closest('li[data-term]');
        if (li) lookup(decodeURIComponent(li.dataset.term));
      }});
      document.getElementById('search-box').addEventListener('input', renderSidebar);
      document.getElementById('sort-mode').addEventListener('change', renderSidebar);

      async function lookup(term) {{
        if (!term) return;
        selectedTerm = term;
        renderSidebar();
        document.getElementById('search-box').blur();

        const statusEl = document.getElementById('status');
        const vecEl = document.getElementById('vector');
        const neighborsEl = document.getElementById('neighbors');
        statusEl.textContent = 'loading...';
        vecEl.textContent = '';
        neighborsEl.innerHTML = '';
        try {{
          const [embResp, neighResp] = await Promise.all([
            fetch('/embedding/' + encodeURIComponent(term)),
            fetch('/neighbors/' + encodeURIComponent(term) + '?k=15'),
          ]);
          if (!embResp.ok) {{
            statusEl.textContent = embResp.status === 404 ? `no such term: "${{term}}"` : `error ${{embResp.status}}`;
            return;
          }}
          const emb = await embResp.json();
          statusEl.textContent = `"${{emb.term}}" (row ${{emb.row}}, ${{emb.dim}} dims):`;
          vecEl.textContent = JSON.stringify(emb.vector);

          const neigh = await neighResp.json();
          neighborsEl.innerHTML = neigh.neighbors.map(nb =>
            `<li data-term="${{encodeURIComponent(nb.term)}}">` +
            `<span>${{nb.term}}</span><span class="sim">${{nb.similarity.toFixed(4)}}</span></li>`
          ).join('');
        }} catch (e) {{
          statusEl.textContent = 'request failed: ' + e;
        }}
      }}
      document.getElementById('neighbors').addEventListener('click', (e) => {{
        const li = e.target.closest('li[data-term]');
        if (li) lookup(decodeURIComponent(li.dataset.term));
      }});

      fetch('/terms/full').then(r => r.json()).then(data => {{
        ALL_TERMS = data;
        renderSidebar();
      }});
    </script>
  </body></html>
  """


def main():
  """Entry point for `uv run infordist-pca-server <pca-dir> [port]` --

  PCA_DIR/app are already resolved/loaded at import time (above), so
  this just launches uvicorn; use `uvicorn infordist_pca.server:app`
  directly instead if you want uvicorn's own CLI flags (--reload etc).
  """
  import uvicorn

  port = int(sys.argv[2]) if len(sys.argv) > 2 else 8001
  uvicorn.run(app, host="0.0.0.0", port=port)


if __name__ == "__main__":
  main()
