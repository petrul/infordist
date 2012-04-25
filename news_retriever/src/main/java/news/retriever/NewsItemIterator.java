package news.retriever;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import news.retriever.google.GoogleNewsTopicPage;
import news.retriever.util.HttpClientWrapper;
import news.retriever.util.UrlUtil;
import news.retriever.util.XpathUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NewsItemIterator implements Iterator<NewsItem> {
	URL rssUrl;
	HttpClientWrapper httpClient = new HttpClientWrapper();
	XpathUtil xpathUtil = new XpathUtil();
	
	public NewsItemIterator(URL rssUrl) {
		this.rssUrl = rssUrl;

		String responseBody = httpClient.getPage(rssUrl);
		Document doc = this.xpathUtil.stringXml2Dom(responseBody);
		List<?> result = this.xpathUtil.xpathAsNodeList(doc, "//item/description/text()");
		
		Pattern pattern = Pattern.compile("(http://news.google.com/.+?)[>]");
		for (Object o : result) {
			String s = ((Node) o).getTextContent();
			int lastIndex = s.lastIndexOf("http");
			String thisIsTheEnd = s.substring(lastIndex);
			Assert.assertTrue(thisIsTheEnd.contains("all"));
			Assert.assertTrue(thisIsTheEnd.contains("news articles"));
			
			Matcher m = pattern.matcher(thisIsTheEnd);
			if (!m.find())
				LOG.warn("could not find all news articles link in [" + s + "]");
			else {
				
				String topicUrl = m.group(1);
				LOG.info("all articles link: "  + topicUrl);
				GoogleNewsTopicPage topic = new GoogleNewsTopicPage(UrlUtil.string2Url(topicUrl));
				for (URL _url : topic.getAllArticleUrls()) {
					LOG.info("suburl : " + _url);
				}
			}
		}
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public NewsItem next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
	}

	Logger LOG = Logger.getLogger(NewsItemIterator.class);
}
