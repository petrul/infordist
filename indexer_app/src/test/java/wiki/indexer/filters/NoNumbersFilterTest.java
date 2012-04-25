package wiki.indexer.filters;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

/**
 * makes sure that {@link NoNumbersFilter} actually does its job.
 * @author dadi
 *
 */
public class NoNumbersFilterTest {
	
	@Test
	public void testNoNumbers() throws Exception {
		TokenStream ts = new MyTokenStream("1984 i&s a ni,ce futu|ristic no/vel these four tokens pass");
		
		NoNumbersFilter filter = new NoNumbersFilter(ts);
		
		Set<Token> tokens = new HashSet<Token>();
		Token tk = new Token();
		while (filter.next(tk) != null) {
			assertFalse(tk.term().equals("1984"));
//			LOG.info(tk.toString());
			tokens.add(tk);
		}
		assertEquals(4, tokens.size());
	}
	
	Logger LOG = Logger.getLogger(NoNumbersFilterTest.class);
}


class MyTokenStream extends TokenStream {
	
	String[] text;
	int i = 0;
	
	public MyTokenStream(String s) {
		this.text = s.split(" ");
	}
	
	
	@Override
	public Token next(Token reusableToken) throws IOException {
		if (i >= text.length) return null;
		
		reusableToken.setTermBuffer(text[i]);
		i++;
		return reusableToken;
		
	}
	
}