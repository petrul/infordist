package news.retriever.db;

import news.retriever.NewsItem;

public interface NewsDb {
	
	/**
	 * look into the persistent store to see if this item was not already downloaded.
	 */
	boolean isAlreadyDownloaded(NewsItem item);
	
}
