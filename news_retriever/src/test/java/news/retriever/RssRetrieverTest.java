package news.retriever;

import static org.junit.Assert.*;

import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Test;

public class RssRetrieverTest {

	@Test
	public void testItems() throws Exception {
		RssRetriever rssRetriever = new RssRetriever(new URL("http://news.google.com/news?pz=1&ned=us&hl=en&output=rss"));
		int counter = 0;
		for (NewsItem item : rssRetriever) {
			LOG.info(item);
			counter++;
		}
		assertTrue(counter > 1);
	}

	Logger LOG = Logger.getLogger(RssRetrieverTest.class);
}
