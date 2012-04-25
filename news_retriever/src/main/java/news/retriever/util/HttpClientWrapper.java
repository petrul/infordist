package news.retriever.util;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpClientWrapper {
	HttpClient httpClient;

	public HttpClientWrapper() {
		HttpClient client = new HttpClient();
		client.getParams().setParameter(HttpMethodParams.USER_AGENT,
				"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8) Gecko/20051111 Firefox/1.5 BAVM/1.0.0");
		this.httpClient = client;

	}

	public String getPage(URL url) {
		return this.getPage(url.toExternalForm());
	}
	
	public String getPage(String urlPart) {
		try {
			GetMethod getMethod = new GetMethod(urlPart);
			int statusCode = this.httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Method failed: " + getMethod.getStatusLine());
			}

			String responseBody = getMethod.getResponseBodyAsString();
			return responseBody;
		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
}
