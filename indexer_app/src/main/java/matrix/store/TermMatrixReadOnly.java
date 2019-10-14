package matrix.store;

import java.io.File;

/**
 * {@link TermMatrix} based on {@link NioFileMatrixStore} for storing data.
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
		// 2nd arg is null because were not initializing anything, the variables if any are read from existing
		super(terms, null, dir, cacheSize, originalComment);
	}

	public TermMatrixReadOnly(String[] terms, File dir, String originalComment) {
		super(terms, null, dir, originalComment);
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
