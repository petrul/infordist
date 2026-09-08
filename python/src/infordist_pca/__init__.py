"""GPU-accelerated truncated eigendecomposition ("PCA") for the big

symmetric term co-occurrence matrices produced by infordist's
indexer_app (matrix.store.NioFileMatrixStore / AbstractTermMatrix).

See main.py for the actual algorithm and CLI.
"""
from .main import main

__all__ = ["main"]
