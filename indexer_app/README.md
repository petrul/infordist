# Infordist indexer

`indexer_app` turns Wikipedia XML dumps into data for distributional-semantic
experiments. It can:

- parse MediaWiki XML dumps without loading the full dump into memory;
- read either plain XML or bzip2-compressed (`.xml.bz2`) dumps;
- build a Lucene positional index with per-document term vectors;
- analyze English or Romanian Wikipedia text with language-specific Lucene
  Snowball stemming and stop words;
- derive term-frequency/co-occurrence matrices for Normalized Google Distance
  (NGD)-style semantic neighborhoods;
- extract compressed context "gists" and compare them with Normalized
  Compression Distance (NCD).

The codebase is an older research project and intentionally retains Lucene
2.4 index formats and APIs. The build itself uses Java 25 and Gradle 9.7.1.

## Pipeline

```text
Wikipedia XML/XML.bz2
        |
        v
language-aware Lucene positional index
        |                         |
        v                         v
term co-occurrence matrix     compressed context gists
        |                         |
        v                         v
NGD neighborhoods            NCD comparisons
```

The Wikipedia indexer stores one Lucene document per page. The page title is
stored as its identifier; page text is tokenized, filtered, lower-cased,
stemmed, and stored as a positional term vector. URLs and numeric tokens are
discarded by the custom Wikipedia tokenizer/filter chain.

## Requirements and build

- JDK 25
- no system Gradle installation is required; use the checked-in wrapper

```bash
cd indexer_app
./gradlew clean test installDist
```

The runnable distribution is created in `build/install/indexer_app/`. Its
`bin/` directory contains `wikipedia-indexer` and the matrix/NCD helper tools.

## Index Wikipedia

`wikipedia-indexer` accepts:

- `-x <file>`: required Wikipedia XML or `.bz2` dump;
- `-o <directory>`: output index directory (a random name is used otherwise);
- `-l, --language <language>`: `English`/`en` (default) or `Romanian`/`ro`;
- `-n, --max-pages <count>`: stop after this many pages, useful for smoke tests.

The output directory is replaced if it already exists, so choose it carefully.

English Wikipedia:

```bash
build/install/indexer_app/bin/wikipedia-indexer \
  -x /data/enwiki-pages-articles.xml.bz2 \
  -o index-enwiki
```

Romanian Wikipedia from the local Conventum download:

```bash
build/install/indexer_app/bin/wikipedia-indexer \
  -x /home/petru/work/conventum/rowiki-2026-08-01-p1p3396124.xml.bz2 \
  -o index-rowiki \
  --language ro
```

Before a full run, index a small sample:

```bash
build/install/indexer_app/bin/wikipedia-indexer \
  -x /home/petru/work/conventum/rowiki-2026-08-01-p1p3396124.xml.bz2 \
  -o /tmp/index-rowiki-smoke \
  --language ro \
  --max-pages 1000
```

## Build an NGD/co-occurrence matrix

`ExtractTermFrequenciesMatrixFromPositionalIndex` reads the positional index,
keeps the most frequent vocabulary terms, and counts co-occurrences in token
windows. For example:

```bash
build/install/indexer_app/bin/ExtractTermFrequenciesMatrixFromPositionalIndex \
  -i index-rowiki \
  -n 40000 \
  -o ngd-rowiki-40k
```

The most important options are:

- `-i`: input Lucene index;
- `-n`: maximum vocabulary size (30,000-50,000 is typical but expensive);
- `-o`: output matrix directory;
- `-f`: minimum term frequency;
- `-d`: maximum number of documents to inspect;
- `-w`: token-window size.

Use the generated `term_matrix_get_terms`, `term_matrix_get_row`, and
`term_matrix_get_neighbours` scripts to inspect matrix contents and semantic
neighbors.

## NCD tools

`retrieve-gists-from-positional-index` extracts a compressed context for each
frequent term from an existing positional index. `ncd_for_two` compares two
files, while `ncd_for_dir` computes pairwise normalized compression distances
for a directory of inputs. Run any generated command with `-h` where available
to see its exact options.

These calculations are CPU-, memory-, and disk-intensive on a full Wikipedia
dump. Start with `--max-pages`, a smaller vocabulary, or a document limit to
confirm the pipeline and estimate resource use.
