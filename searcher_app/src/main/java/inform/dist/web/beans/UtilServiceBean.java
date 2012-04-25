package inform.dist.web.beans;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class UtilServiceBean {

	private static final long serialVersionUID = 1L;
	
	public Map<String, String> urlEncode = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		@Override
		public String get(Object key) {
			return urlEncode((String) key);
		}
	};

	public Map<String, String> latin2utf8 = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;

		@Override
		public String get(Object key) {
			return latin2Utf8((String) key);
		}
	};
	
	
	public Map<String, String> getLatin2utf8() {
		return latin2utf8;
	}

	public Map<String, String> getUrlEncode() {
		return urlEncode;
	}

	public String urlEncode(String what) {
		try {
			return URLEncoder.encode(what,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String latin2Utf8(String s) {
		try {
			return new String(s.getBytes("ISO-8859-1"),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
