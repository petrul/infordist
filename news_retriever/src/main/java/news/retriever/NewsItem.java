package news.retriever;

import java.net.URL;

public class NewsItem {

	protected NewsSource source;
	protected URL url;

	public NewsItem(NewsSource source, URL url) {
		this.source = source;
		this.url = url;
	}

	public String getTitle() {
		throw new RuntimeException();
	}

	/**
	 * @return a String identificator, usually based on the id, which is
	 *         garanteed to be unique within the parent source
	 */
	public String getUniqueId() {
		return this.url.toExternalForm();
	}
}
