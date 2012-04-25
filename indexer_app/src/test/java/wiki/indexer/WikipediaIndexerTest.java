package wiki.indexer;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import wiki.indexer.cli.WikipediaIndexer;

public class WikipediaIndexerTest {

	@Test
	public void testRun() throws Exception {
		String rndStr = RandomStringUtils.randomAlphabetic(5);
		String idxDirName = "/tmp/idx/idx-wiki-" + rndStr;
		String path = this.getClass().getClassLoader().getResource("wikipedia-excerpt.xml").getPath();
		
		WikipediaIndexer wikipediaIndexer = new WikipediaIndexer(path, idxDirName);
//		wikipediaIndexer.setChunkBy(WikipediaIndexer.ChunkBy.SENTENCE);
		wikipediaIndexer.setUseShutDownHook(false);
		wikipediaIndexer.run();
		
		FileUtils.deleteDirectory(new File(idxDirName));
	}

}
