package matrix.store;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * {@link TermMatrix} based on {@link NioFileMatrixStore} for storing data.
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

	static Logger LOG = Logger.getLogger(TermMatrixRW.class);

}
