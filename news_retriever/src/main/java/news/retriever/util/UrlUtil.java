package news.retriever.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {
	public static URL string2Url(String s) {
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
