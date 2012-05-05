package store.termmatrix;


import store.matrix.IntMatrixStore;
import store.matrix.NioFileMatrixStore;

import java.io.File;

/**
 * {@link TermMatrix} based on {@link matrix.store.NioFileMatrixStore} for storing data.
 * 
 * @author dadi
 */
public class TermMatrixReadOnly extends AbstractTermMatrix {

	/* don't use this */
	public TermMatrixReadOnly() {
	}
	
	public TermMatrixReadOnly(File dir) {
		super(dir);
	}

	public TermMatrixReadOnly(String[] terms, File dir, int cacheSize, String originalComment) {
		super(terms, dir, cacheSize, originalComment);
	}

	public TermMatrixReadOnly(String[] terms, File dir, String originalComment) {
		super(terms, dir, originalComment);
	}

	@Override
	protected IntMatrixStore newIntMatrixStore(File file, int rows, int columns, int cacheSize) {
		return new NioFileMatrixStore(file, rows, columns, cacheSize, "r");
	}

	@Override
	public void setComplexity(String term, int value) {
		throw new RuntimeException("you may not write to this read-only matrix");
	}

	@Override
	public void setCombinedComplexity(String term1, String term2, int value) {
		throw new RuntimeException("you may not write to this read-only matrix");
	}

}
