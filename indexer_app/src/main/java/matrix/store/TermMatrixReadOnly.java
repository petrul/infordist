package matrix.store;

import java.io.File;

/**
 * {@link TermMatrix} based on {@link NioFileMatrixStore} for storing data.
 * 
 * @author dadi
 */
public class TermMatrixReadOnly extends AbstractTermMatrix {

	public TermMatrixReadOnly(File dir, int cacheSize) {
		super.initFromExisting(dir, cacheSize);
	}

	public TermMatrixReadOnly(File dir) {
		this(dir, 8);
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
