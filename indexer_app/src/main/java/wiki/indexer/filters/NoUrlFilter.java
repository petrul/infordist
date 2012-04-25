package wiki.indexer.filters;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.wikipedia.analysis.WikipediaTokenizer;


/**
 * Filters out URLs
 * @author dadi
 *
 */
public class NoUrlFilter extends TokenFilter {

	private static final long serialVersionUID = 1L;

	public NoUrlFilter(TokenStream in) {
		super(in);
	}
	
	  /**
	   * Returns the next input Token whose term() is not a stop word.
	   */
	  public final Token next(final Token reusableToken) throws IOException {
	    assert reusableToken != null;
	    for (Token nextToken = input.next(reusableToken); nextToken != null; nextToken = input.next(reusableToken)) {
	    	String type = nextToken.type();
	    	if (! WikipediaTokenizer.EXTERNAL_LINK_URL.equals(type) && ! "<HOST>".equals(type) ) {
	    		return nextToken;
	    	}
	    }
	    // reached EOS -- return null
	    return null;
	  }
}
