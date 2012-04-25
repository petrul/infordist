package matrix.store;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import junit.framework.Assert;

import matrix.store.AbstractTermMatrix.OpeningStatus;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TermMatrixRWTest {

	File tempDir;
	
	@Before
	public void setUp() {
		this.tempDir = new File(System.getProperty("java.io.tmpdir"), this.getClass().getCanonicalName());
		this.tempDir.mkdirs();
	}
	
	@After
	public void after() {
		FileUtils.deleteQuietly(this.tempDir);
	}
	
	@Test
	public void testGetCombinedComplexity() throws Exception {
		
		
		File f = new File(this.tempDir, RandomStringUtils.randomAlphabetic(10) + ".termmatrix");
		
		int nterms = 1000;
		String[] terms = new String[nterms];
		
		/* generate random terms */
		for (int i = 0; i < nterms; i++) {
			terms[i] = RandomStringUtils.randomAlphabetic(10);
		}
		
		TermMatrixRW termMatrix = new TermMatrixRW(terms, f, "original comment");
		
		int[] crnd = new int[nterms];
		int[][] ccrnd = new int[nterms][nterms];
		
		/* generate random complexity and combined-complexity data */
		Random rnd = new Random();
		for (int i = 0; i < nterms; i++) {
			assertEquals(-1, termMatrix.getComplexity(terms[i]));
			crnd[i] = rnd.nextInt();
			for (int j = 0; j < nterms; j++) {
				assertEquals(-1, termMatrix.getCombinedComplexity(terms[i], terms[j]));
				ccrnd[i][j] = rnd.nextInt();
			}
		}
		
		for (int i = 0; i < nterms; i++) {
			termMatrix.setComplexity(terms[i], crnd[i]);
			for (int j = 0; j < nterms; j++) {
				termMatrix.setCombinedComplexity(terms[i], terms[j], ccrnd[i][j]);
			}
		}
		
		termMatrix.setVariable("num_docs", 70);
		termMatrix.close();
		
		Assert.assertEquals(OpeningStatus.NEW, termMatrix.openingStatus);
		
		termMatrix = new TermMatrixRW(f);
		Assert.assertEquals(OpeningStatus.EXISTING, termMatrix.openingStatus);
		assertEquals(70, termMatrix.getVariable("num_docs"));
		assertEquals(70, termMatrix.getVariable("num_docs")); // two times, so caching works
		for (int i = 0; i < nterms; i++) {
			assertEquals(crnd[i], termMatrix.getComplexity(terms[i]));
			for (int j = 0; j < nterms; j++) {
				assertEquals(ccrnd[i][j], termMatrix.getCombinedComplexity(terms[i], terms[j])); 
			}
		}
		termMatrix.close();
		
		File cfile = termMatrix.getComplexitiesBinFilename();
		long cfileLength = cfile.length();
		assertEquals(Integer.SIZE / 8 * nterms, cfileLength);
//		LOG.info("c " + cfileLength);
		File ccfile = termMatrix.getCombinedComplexitiesBinFilename();
		long ccfileLength = ccfile.length();
		assertEquals(Integer.SIZE / 8 * nterms * nterms, ccfileLength);
//		LOG.info("cc " + ccfileLength);
		
		FileUtils.deleteDirectory(f);
	}

	static Logger LOG = Logger.getLogger(TermMatrixRWTest.class);
}
