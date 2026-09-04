package matrix.store;

import java.io.File;
import java.util.Random;

import org.junit.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class NioFileMatrixStoreTest {

	@Test
	public void testWriteCell() {
		File f = new File("/tmp/" + RandomStringUtils.randomAlphabetic(10) + ".binarymatrix");
		try { f.delete(); } catch (Exception e) { ; }
		int rows = 1000;
		int cols = 1000;
		
		Random rnd = new Random();
		int[][] m = new int[rows][cols];
		for (int i = 0; i < rows;i++)
			for (int j = 0; j < cols; j++) 
				m[i][j] = rnd.nextInt();
		
		LOG.info("starting");
		
		NioFileMatrixStore matrix = new NioFileMatrixStore(f, rows, cols, "rw");
		matrix.init(-1);
		
		for (int i = 0; i < rows;i++)
			for (int j = 0; j < cols; j++) 
				matrix.put(i, j, m[i][j]);
		
		matrix.close();
		
		matrix = new NioFileMatrixStore(f , rows, cols, "rw");
		for (int i1 = 0; i1 < rows; i1++) 
			for (int j = 0; j < cols; j++) 
				Assert.assertEquals(m[i1][j], matrix.get(i1, j));
		
		matrix.close();
		LOG.info("done");
		f.delete();
	}

	/**
	 * Isolates the real bug behind the "known bug ... Negative position"

	 * workaround in ExtractTermFrequenciesMatrixFromPositionalIndex: for a
	 * 55000-term matrix (the actual failing run), row i=54999's naive
	 * "int position = BYTES_PER_INT * i * columns" is 4 * 54999 * 55000 =
	 * 12,099,780,000, which overflows a 32-bit int (max 2,147,483,647) and
	 * wraps negative -- FileChannel.map/write then rejects it with
	 * IllegalArgumentException("Negative position"). Tests the pure
	 * position/rowsize math directly rather than actually writing a
	 * ~12GB matrix file, which a real n=55000 run would require on disk.
	 */
	@Test
	public void testComputePositionDoesNotOverflowForA55000TermMatrix() {
		int columns = 55000;
		int lastRow = columns - 1;

		long expectedPosition = 4L * lastRow * columns;
		long actualPosition = NioFileMatrixStore.computePosition(0L, 4, lastRow, columns);

		Assert.assertEquals(expectedPosition, actualPosition);
		Assert.assertTrue("position must never be negative", actualPosition >= 0);

		// Sanity check this scenario really would have overflowed a plain
		// 32-bit int multiplication, i.e. that this test is exercising the
		// actual bug and not a case that was always fine.
		int naiveIntPosition = 4 * lastRow * columns;
		Assert.assertTrue(
				"expected the naive int computation to overflow negative for this scenario"
						+ " (if it didn't, this test no longer reproduces the original bug)",
				naiveIntPosition < 0);
	}

	@Test
	public void testComputeRowSizeIsCorrectForA55000TermMatrix() {
		int columns = 55000;
		long expectedRowSize = 4L * columns;
		Assert.assertEquals(expectedRowSize, NioFileMatrixStore.computeRowSize(4, columns));
	}

	static Logger LOG = Logger.getLogger(NioFileMatrixStoreTest.class);
}
