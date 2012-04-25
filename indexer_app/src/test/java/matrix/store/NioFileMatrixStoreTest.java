package matrix.store;

import java.io.File;
import java.util.Random;

import junit.framework.Assert;

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

	static Logger LOG = Logger.getLogger(NioFileMatrixStoreTest.class);
}
