package store.termmatrix;

import java.io.File;
import java.util.logging.Logger;

import store.matrix.IntMatrixStore;
import store.matrix.NioFileMatrixStore;

/**
 * {@link TermMatrix} based on {@link matrix.store.NioFileMatrixStore} for storing data.
 * @author dadi
 */
public class TermMatrixRW extends AbstractTermMatrix {

	public TermMatrixRW(File dir) {
		super(dir);
	}

	public TermMatrixRW(String[] terms, File dir, int cacheSize, String originalComment) {
		super(terms, dir, cacheSize, originalComment);
	}

	public TermMatrixRW(String[] terms, File dir, String originalComment) {
		super(terms, dir, originalComment);
	}

	@Override
	protected IntMatrixStore newIntMatrixStore(File file, int rows, int columns, int cacheSize) {
		return new NioFileMatrixStore(file, rows, columns, cacheSize, "rw");
	}

	static Logger LOG = Logger.getLogger(TermMatrixRW.class.getCanonicalName());

}
