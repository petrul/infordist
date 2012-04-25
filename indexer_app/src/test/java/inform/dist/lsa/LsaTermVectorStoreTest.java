package inform.dist.lsa;

import java.io.File;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LsaTermVectorStoreTest {
	
	File tempDir;
	
	@Before
	public void setUp() {
		this.tempDir = new File(System.getProperty("java.io.tmpdir"), this.getClass().getCanonicalName() + RandomStringUtils.randomAlphabetic(10));
		this.tempDir.mkdirs();
	}
	
	@After
	public void after() {
		FileUtils.deleteQuietly(this.tempDir);
	}

	@Test
	public void testLsaTermVectorStore() {
		//fail("Not yet implemented");
		int nterms = 2000;
		int ndocs = 4000;
		
		String[] terms = new String[nterms];
		
		/* generate random terms */
		for (int i = 0; i < nterms; i++) {
			terms[i] = RandomStringUtils.randomAlphabetic(10);
		}
		
		Random rnd = new Random();
		long[][] termVectors = new long[nterms][ndocs];
		for (int i = 0; i < nterms; i++)
			for (int j = 0; j < ndocs; j++)
				termVectors[i][j] = Math.abs(rnd.nextInt());
					
		File dir = new File(this.tempDir, "abc");
		new LsaTermVectorStore(dir, terms, termVectors); // creator constructor
		
		
		LsaTermVectorStore store = new LsaTermVectorStore(dir); // reader constructor
		
		for (int i = 0; i < terms.length; i++) {
			int[] vect = store.getVector(terms[i]);
			Assert.assertNotNull(vect);
			
			assertArraysAreEqual(termVectors[i], vect);
		}			
	}

	private void assertArraysAreEqual(long[] longs, int[] ints) {
		Assert.assertEquals(longs.length, ints.length);
		for (int i = 0; i < longs.length; i++) {
			Assert.assertEquals(longs[i], ints[i]);
		}
	}

}
