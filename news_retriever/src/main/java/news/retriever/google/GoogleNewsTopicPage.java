package news.retriever.google;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import news.retriever.util.HttpClientWrapper;
import news.retriever.util.UrlUtil;
import news.retriever.util.XpathUtil;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A topic in the google news rss corresponds to lots of pages of news on that
 * topic.
 * 
 * For instance, as of avril 20th 2009, the following url is such a topic page
 * http://news.google.com/news?pz=1&ned=us&hl=en&ncl=1337195624
 * 
 * This code knows how to click next on those pages and get links from all the pages.
 * 
 * @author dadi
 * 
 */
public class GoogleNewsTopicPage {
	URL url;
	HttpClientWrapper httpClient = new HttpClientWrapper();
	XpathUtil xpathutil = new XpathUtil();

	public GoogleNewsTopicPage(URL url) {
		this.url = url;
	}

	public List<URL> getAllArticleUrls() {
		try {
			ArrayList<URL> urls = new ArrayList<URL>();
			
			String crtUrl = url.toExternalForm();
			int pageCounter = 0;
			while (crtUrl != null) {
				String page = this.httpClient.getPage(crtUrl);
				Document dom = this.xpathutil.stringHtml2Dom(page);
				String xpath_string = "//div[@id='story-articles']//h2/a";
				List<Node> nodeList = this.xpathutil.xpathAsNodeList(dom, xpath_string);
				
				if (nodeList == null || nodeList.size() == 0)
					LOG.warn("no articles retrieved from [" + crtUrl + "]");
				
				for (Object n : nodeList) {
					Element elem = (Element) n;
					String href = elem.getAttribute("href");
					urls.add(UrlUtil.string2Url(href));
				}
				List<Node> next = this.xpathutil.xpathAsNodeList(dom, "//div[@id='pagination']//td[@class='next']//a");
				if (next.size() > 0) {
					Element nextPage = (Element)next.get(0);
					String nextPageUrl = nextPage.getAttribute("href");
					if (nextPageUrl != null)
						crtUrl = "http://news.google.com" + nextPageUrl;
					else 
						crtUrl = null;
				} else
					crtUrl = null;
				pageCounter++;
			}
			
			LOG.info("there were " + pageCounter + " pages for this topic");
			return urls;
		} 
		finally {}
	}
	
	Logger LOG = Logger.getLogger(GoogleNewsTopicPage.class);
}
