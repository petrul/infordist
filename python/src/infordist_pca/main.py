"""GPU-accelerated truncated eigendecomposition ("PCA") of the big

symmetric term co-occurrence matrix produced by infordist's indexer_app
(ExtractTermFrequenciesMatrixFromPositionalIndex -> matrix.store's
combined-complexities.binarymatrix).

Uses the randomized range-finder algorithm (Halko/Martinsson/Tropp)
rather than a full O(n^3) eigendecomposition: for n=55000 -> k=1000
components, full eigendecomposition needs the whole matrix in RAM/on the
GPU at once (~24GB as float32) and is O(n^3). This instead streams
row-blocks of the disk-backed matrix through the GPU a few times
(1 + power_iters + 1 passes total), never holding more than a
n x (k+oversample) basis matrix resident anywhere -- for k=1000 that's
~220MB regardless of how big n is, so it works the same way whether n is
5000 (the local test fixture) or 55000 (a real run).
"""
import argparse
import json
import re
import time
from pathlib import Path
from urllib.parse import unquote

import numpy as np
import torch

COMBINED_COMPLEXITIES_FILE = "combined-complexities.binarymatrix"
COMPLEXITIES_FILE = "complexities.binarymatrix"  # 1 x n row of per-term absolute frequency
TERMS_FILE = "terms.mat"

# Java's ByteBuffer/MappedByteBuffer default to big-endian, and
# matrix.store.NioFileMatrixStore never calls .order(...), so every
# *.binarymatrix file indexer_app writes is raw big-endian int32,
# row-major, with no header -- confirmed by reading that class directly.
DTYPE_ON_DISK = np.dtype(">i4")


def infer_matrix_size(path: Path) -> int:
  """Recovers n for an n x n matrix purely from the file's byte size --

  there's no header to read, so this (or an explicit --n) is the only
  way to know n; terms.mat is treated as optional/best-effort labels
  only, never required.
  """
  nbytes = path.stat().st_size
  n_elems = nbytes // DTYPE_ON_DISK.itemsize
  if n_elems * DTYPE_ON_DISK.itemsize != nbytes:
    raise ValueError(f"{path}: size {nbytes} isn't a whole number of int32 elements")
  n = int(round(n_elems ** 0.5))
  if n * n != n_elems:
    raise ValueError(f"{path}: {n_elems} elements isn't a perfect square (not n x n)")
  return n


def load_term_frequencies(matrix_dir: Path, n: int) -> np.ndarray | None:
  """Reads complexities.binarymatrix -- AbstractTermMatrix's "cStore", a

  1 x n row of each term's absolute frequency (set via setComplexity),
  same raw big-endian int32 encoding as the main n x n matrix. Small
  (n x 4 bytes) so it's just read fully rather than memmapped. Returns
  None (never raises) if missing -- used only to let the UI sort terms
  by frequency, not required for the PCA itself.
  """
  path = matrix_dir / COMPLEXITIES_FILE
  if not path.exists():
    return None
  freqs = np.fromfile(path, dtype=DTYPE_ON_DISK, count=n)
  if freqs.shape[0] != n:
    return None
  return freqs.astype(np.int64)


def open_matrix_memmap(path: Path, n: int | None = None) -> np.memmap:
  """Opens a combined-complexities.binarymatrix file as a lazily-paged,

  read-only view -- the file is never fully loaded into RAM; row blocks
  are paged in on demand as the caller actually reads them.
  """
  if n is None:
    n = infer_matrix_size(path)
  return np.memmap(path, dtype=DTYPE_ON_DISK, mode="r", shape=(n, n))


def load_terms(terms_mat_path: Path) -> list[str] | None:
  """Best-effort parse of the "terms" string-array section of a

  MatTextFileExporter-produced terms.mat file (index<TAB>"term" lines).
  Returns None (never raises) if the file is missing or doesn't match
  the expected shape -- term labels are a nice-to-have for the saved
  output, not required for the PCA itself.
  """
  if not terms_mat_path.exists():
    return None
  try:
    text = terms_mat_path.read_text(errors="replace")
    # terms is always the last section in a real terms.mat -- the file
    # ends right at EOF with no trailing blank line, so the closing
    # "\n\n" that separates every other section must also accept end of
    # string here (confirmed against a real indexer_app-produced file).
    match = re.search(r"# name:terms\b.*?# size:(\d+).*?\n\n(.*?)(?:\n\n|\Z)", text, re.DOTALL)
    if not match:
      return None
    size = int(match.group(1))
    terms: list[str | None] = [None] * size
    for line in match.group(2).splitlines():
      idx_str, _, rest = line.partition("\t")
      term = rest.strip().strip('"')
      # Indexer_app percent-encodes non-ASCII bytes into the term strings
      # it writes (e.g. "%C3%AEncep" for "încep", "rom%C3%A2n" for
      # "român") and never decodes them back -- unquote() is a no-op
      # on any term that has no "%" in it, so this is safe to apply
      # unconditionally rather than only to the terms that need it.
      terms[int(idx_str)] = unquote(term)
    if any(t is None for t in terms):
      return None
    return terms
  except Exception:
    return None


def _streaming_matmul(
    matrix: np.memmap,
    right: torch.Tensor,
    chunk_rows: int,
    device: torch.device,
    dtype: torch.dtype,
    progress_label: str,
) -> torch.Tensor:
  """Computes matrix @ right, reading `matrix` (a big, disk-backed n x n

  memmap) in row-blocks so the full matrix is never resident in host RAM
  or GPU memory at once -- only one `chunk_rows x n` slice at a time.
  `.astype(np.float32)` on a big-endian memmap slice transparently
  handles both the byte-swap and the int32->float32 conversion; only the
  bytes for that slice get paged in from disk.
  """
  n = matrix.shape[0]
  out = torch.empty((n, right.shape[1]), dtype=dtype, device=device)
  t0 = time.monotonic()
  for start in range(0, n, chunk_rows):
    end = min(start + chunk_rows, n)
    chunk = np.asarray(matrix[start:end]).astype(np.float32)
    chunk_t = torch.from_numpy(chunk).to(device, dtype=dtype, non_blocking=True)
    out[start:end] = chunk_t @ right
  print(f"      [{progress_label}] streamed {n} rows in {time.monotonic() - t0:.1f}s")
  return out


def randomized_symmetric_eig(
    matrix: np.memmap,
    k: int,
    oversample: int = 10,
    n_power_iter: int = 2,
    chunk_rows: int = 4000,
    device: str | torch.device = "cuda",
    dtype: torch.dtype = torch.float32,
    seed: int | None = 0,
) -> tuple[torch.Tensor, torch.Tensor]:
  """Randomized truncated eigendecomposition of a large symmetric matrix

  (Halko/Martinsson/Tropp range-finder + Rayleigh-Ritz), streaming
  row-blocks of `matrix` instead of ever materializing the whole thing.
  Total cost is O(n^2 * (k+oversample)) across (2 + n_power_iter)
  streaming passes over the matrix, vs. O(n^3) for a full
  eigendecomposition -- for n=55000, k=1000 that's roughly 55x less
  work, and the working set (the n x (k+oversample) basis) is a few
  hundred MB regardless of n.

  Returns (eigenvalues, eigenvectors) for the top-k eigenvalues by
  magnitude, eigenvalues descending, eigenvectors as columns (n, k).
  """
  n = matrix.shape[0]
  device = torch.device(device)
  L = k + oversample

  generator = torch.Generator(device=device)
  if seed is not None:
    generator.manual_seed(seed)
  omega = torch.randn((n, L), dtype=dtype, device=device, generator=generator)

  q = _streaming_matmul(matrix, omega, chunk_rows, device, dtype, "range-finder")
  q, _ = torch.linalg.qr(q)

  for i in range(n_power_iter):
    q = _streaming_matmul(matrix, q, chunk_rows, device, dtype, f"power-iter {i + 1}/{n_power_iter}")
    q, _ = torch.linalg.qr(q)

  aq = _streaming_matmul(matrix, q, chunk_rows, device, dtype, "final-projection")
  b = q.T @ aq  # (L, L) -- small and cheap, pure GPU op, no more disk streaming
  b = 0.5 * (b + b.T)  # guard against float round-off asymmetry before eigh

  eigvals_small, eigvecs_small = torch.linalg.eigh(b)  # ascending order
  order = torch.argsort(eigvals_small.abs(), descending=True)[:k]
  eigvals = eigvals_small[order]
  eigvecs = q @ eigvecs_small[:, order]  # approx eigenvectors of the full matrix

  order2 = torch.argsort(eigvals, descending=True)
  return eigvals[order2], eigvecs[:, order2]


def build_arg_parser() -> argparse.ArgumentParser:
  parser = argparse.ArgumentParser(
      description="GPU-accelerated truncated eigendecomposition (\"PCA\") of an "
                  "infordist term co-occurrence matrix (combined-complexities.binarymatrix)."
  )
  parser.add_argument(
      "matrix_dir", type=Path,
      help="directory produced by ExtractTermFrequenciesMatrixFromPositionalIndex "
           "(contains combined-complexities.binarymatrix and terms.mat)",
  )
  parser.add_argument("-k", "--components", type=int, default=1000,
                       help="number of components to keep (default: 1000)")
  parser.add_argument("--oversample", type=int, default=10,
                       help="extra random directions for accuracy margin (default: 10)")
  parser.add_argument("--power-iters", type=int, default=2,
                       help="power-iteration refinement passes (default: 2; more = more "
                            "accurate but one more streaming pass over the matrix each)")
  parser.add_argument("--chunk-rows", type=int, default=4000,
                       help="rows streamed to the GPU per batch (default: 4000; lower this "
                            "if you hit CUDA out-of-memory)")
  parser.add_argument("--device", default="cuda" if torch.cuda.is_available() else "cpu",
                       help="torch device (default: cuda if available, else cpu)")
  parser.add_argument("-o", "--outdir", type=Path, default=None,
                       help="output directory (default: <matrix_dir>/pca) -- written as "
                            "components.npy (n x k, directly memmap-able), eigenvalues.npy, "
                            "and terms.json (term -> row index), for embeddings_server.py to serve")
  return parser


COMPONENTS_FILE = "components.npy"
EIGENVALUES_FILE = "eigenvalues.npy"
TERMS_INDEX_FILE = "terms.json"
FREQUENCIES_FILE = "frequencies.npy"


def save_embeddings(
    outdir: Path,
    eigenvalues: np.ndarray,
    components: np.ndarray,
    terms: list[str] | None,
    frequencies: np.ndarray | None = None,
):
  """Saves as plain .npy (not .npz, which is a zip archive and can't be

  memmapped) so components.npy -- the actual per-word embeddings, and by
  far the biggest of the three files -- can be opened later with
  np.load(path, mmap_mode='r') instead of ever being fully loaded into
  RAM, the same lazy-paging property the input matrix itself has.
  """
  outdir.mkdir(parents=True, exist_ok=True)
  np.save(outdir / COMPONENTS_FILE, components)
  np.save(outdir / EIGENVALUES_FILE, eigenvalues)
  if terms is not None:
    (outdir / TERMS_INDEX_FILE).write_text(
        json.dumps({term: i for i, term in enumerate(terms)}, ensure_ascii=False)
    )
  if frequencies is not None:
    np.save(outdir / FREQUENCIES_FILE, frequencies)


def load_embeddings(
    outdir: Path,
) -> tuple[np.memmap, np.ndarray, dict[str, int] | None, np.ndarray | None]:
  """Opens a directory written by save_embeddings. components is a lazy

  memmap (mmap_mode="r"): opening it doesn't read the file, and indexing
  a single row only pages in that row's bytes -- exactly what a REST
  endpoint serving one word's embedding at a time wants, regardless of
  how large n is.
  """
  components = np.load(outdir / COMPONENTS_FILE, mmap_mode="r")
  eigenvalues = np.load(outdir / EIGENVALUES_FILE)
  terms_path = outdir / TERMS_INDEX_FILE
  term_index = json.loads(terms_path.read_text()) if terms_path.exists() else None
  freqs_path = outdir / FREQUENCIES_FILE
  frequencies = np.load(freqs_path) if freqs_path.exists() else None
  return components, eigenvalues, term_index, frequencies


def main():
  args = build_arg_parser().parse_args()

  args.outdir = args.outdir or (args.matrix_dir / "pca")
  matrix_path = args.matrix_dir / COMBINED_COMPLEXITIES_FILE
  if not matrix_path.exists():
    raise SystemExit(f"Error: no {COMBINED_COMPLEXITIES_FILE} in {args.matrix_dir}")

  n = infer_matrix_size(matrix_path)
  size_gb = matrix_path.stat().st_size / 1e9
  print(f"matrix: {matrix_path} ({n} x {n}, {size_gb:.2f} GB on disk)")

  device = torch.device(args.device)
  device_label = str(device)
  if device.type == "cuda":
    if not torch.cuda.is_available():
      raise SystemExit("Error: --device cuda requested but CUDA isn't available in this torch install.")
    device_label += f" ({torch.cuda.get_device_name(device)})"
  print(f"device: {device_label}")

  matrix = open_matrix_memmap(matrix_path, n)
  terms = load_terms(args.matrix_dir / TERMS_FILE)
  if terms is not None and len(terms) != n:
    print(f"      warning: terms.mat has {len(terms)} terms but the matrix is {n} x {n}; ignoring term labels")
    terms = None
  frequencies = load_term_frequencies(args.matrix_dir, n)

  t0 = time.monotonic()
  eigvals, eigvecs = randomized_symmetric_eig(
      matrix,
      k=args.components,
      oversample=args.oversample,
      n_power_iter=args.power_iters,
      chunk_rows=args.chunk_rows,
      device=device,
  )
  elapsed = time.monotonic() - t0
  print(f"done in {elapsed:.1f}s -- {n} x {n} -> {n} x {args.components}")

  save_embeddings(args.outdir, eigvals.cpu().numpy(), eigvecs.cpu().numpy(), terms, frequencies)
  print(f"saved: {args.outdir}")


if __name__ == "__main__":
  main()
