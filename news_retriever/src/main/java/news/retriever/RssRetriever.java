package news.retriever;

import java.net.URL;
import java.util.Iterator;

/**
 * class that manages the Google RSS stream
 * 
 * @author dadi
 *
 */
public class RssRetriever implements Iterable<NewsItem> {

	URL url;

	public RssRetriever(URL url) {
		this.url = url;
	}

	@Override
	public Iterator<NewsItem> iterator() {
		return new NewsItemIterator(this.url);
	}
}
