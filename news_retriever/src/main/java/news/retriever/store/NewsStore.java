package news.retriever.store;


import java.io.File;

import news.retriever.NewsItem;

public class NewsStore {
	
	File dir;
	
	public NewsStore(File dir) {
		this.dir = dir;
	}
	
	public void store(NewsItem item) {
		throw new RuntimeException();
	}
}
