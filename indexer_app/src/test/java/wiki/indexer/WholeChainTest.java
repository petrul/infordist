package wiki.indexer;

import inform.dist.cli.ExtractTermFrequenciesMatrixFromPositionalIndex;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wiki.indexer.cli.WikipediaIndexer;

/**
 * runs indexer, NGD calculations and semantic neighbourhood; no assertions. Just makes sure that
 * no exceptions are thrown.
 * 
 * @author dadi
 *
 */
public class WholeChainTest {
	File tmpdir ;
	
	@Before
	public void before() {
		this.tmpdir = new File(System.getProperties().getProperty("java.io.tmpdir") + "/" + this.getClass().getCanonicalName());
		this.tmpdir.mkdirs();
	}

	
	@After
	public void after() {
		FileUtils.deleteQuietly(this.tmpdir);
	}


	@Test
	public void testWholeChain() throws Exception {
		
		File indexDir =  new File(tmpdir + "/tmptest" + RandomStringUtils.randomAlphabetic(5) + "idx");
		File ngdDir = new File(tmpdir + "/ngdstuff" + RandomStringUtils.randomAlphabetic(5) + "dir");
	
		try {
			
			String xml = this.getClass().getClassLoader().getResource("wikipedia-excerpt.xml").getPath();
			WikipediaIndexer wi = new WikipediaIndexer(xml, indexDir.getAbsolutePath());
			wi.setUseShutDownHook(false);
			wi.run();

			ExtractTermFrequenciesMatrixFromPositionalIndex ngd = new ExtractTermFrequenciesMatrixFromPositionalIndex(
						indexDir.getAbsolutePath(), 
						5,
						1000
//						, CalculateNgdMatrixFromPositionalIndex.OutputFormat.BINARY_NIO
						);
			ngd.setOutDir(ngdDir.getAbsolutePath());
			
			ngd.run();
			LOG.info("complexity results is in: " + ngd);

			
		} finally {
			try {
				FileUtils.deleteDirectory(indexDir);
				FileUtils.deleteDirectory(ngdDir);
			} catch (Exception e) {
				LOG.error(e,e);
			}
		}
	}

	@Test 
	public void testCreateFiles() throws Exception {
		String tmpdir = System.getProperties().getProperty("java.io.tmpdir") + "/" + this.getClass().getCanonicalName();
		File indexDir =  new File(tmpdir + "/tmptest" + RandomStringUtils.randomAlphabetic(5) + "idx");
		File ngdDir = new File(tmpdir + "/ngdstuff" + RandomStringUtils.randomAlphabetic(5) + "dir");
		
		String xml = this.getClass().getClassLoader().getResource("wikipedia-excerpt.xml").getPath();
		WikipediaIndexer wi = new WikipediaIndexer(xml, indexDir.getAbsolutePath());
		wi.setUseShutDownHook(false);
		wi.run();

		ExtractTermFrequenciesMatrixFromPositionalIndex ngd = new ExtractTermFrequenciesMatrixFromPositionalIndex(
					indexDir.getAbsolutePath(), 
					5,
					1000
//					, CalculateNgdMatrixFromPositionalIndex.OutputFormat.BINARY_NIO
					);
		ngd.setOutDir(ngdDir.getAbsolutePath());
		
		ngd.run();
		FileUtils.deleteDirectory(indexDir);
		FileUtils.deleteDirectory(ngdDir);
//		LOG.info("output in :" + ngdDir);
	}
	
	Logger LOG = Logger.getLogger(WholeChainTest.class);
}
