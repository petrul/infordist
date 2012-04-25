package wiki.indexer.filters;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;


/**
 * Filters out URLs
 * @author dadi
 *
 */
public class NoNumbersFilter extends TokenFilter {

	private static final long serialVersionUID = 1L;
	
	public final static String REGEX_STRING = "[\\d\\/&|,]";
//	public final static String REGEX_STRING = "\\d";
	
	Matcher matcher = Pattern.compile(REGEX_STRING).matcher("");
	
	public NoNumbersFilter(TokenStream in) {
		super(in);
	}
	
	  /**
	   * Returns the next input Token whose term() is not a stop word.
	   */
	  public final Token next(final Token reusableToken) throws IOException {
		
	    assert reusableToken != null;
	    
	    for (Token nextToken = input.next(reusableToken); nextToken != null; nextToken = input.next(reusableToken)) {
	    	String text = nextToken.term();
	    	matcher.reset(text);
	    	if (!(
	    		text.length() == 1 // we don't like single letters
	    		|| matcher.find()  // we don't like numbers
	    	))
	    		return nextToken;
	    }
	    // reached EOS -- return null
	    return null;
	  }
}
