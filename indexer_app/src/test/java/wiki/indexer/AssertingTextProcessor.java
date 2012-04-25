package wiki.indexer;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import wiki.indexer.filters.NoNumbersFilter;
import wiki.indexer.tokenizer.CustomWikipediaTokenizer;

/**
 */
class AssertingTextProcessor implements TextProcessor {
	WikipediaSnowballAnalyzer analyzer;
	public AssertingTextProcessor() {
		this.analyzer = new WikipediaSnowballAnalyzer("English");
	}

	@Override
	public void processText(StringBuilder id, StringBuilder textBody) {
		try {
			TokenStream tokenStream = this.analyzer.tokenStream("text", new StringReader(textBody.toString()));
			Token tk = new Token();
			
			while (tokenStream.next(tk) != null) {
				assertFalse(tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
				assertFalse("met http on term [" + tk + "] in " + textBody, tk.term().contains("http"));
				assertFalse("met [blank] on  term [" + tk + "] in " + textBody, tk.term().contains(" "));
				assertFalse("met jpg in term " + tk  + "on  " + textBody, tk.term().contains("jpg"));
				assertFalse("met nbsp on  " + textBody, tk.term().equals("nbsp"));
				assertFalse("met pdf on term " + tk + " in "+ textBody, tk.term().contains("pdf"));
				assertFalse("met redirect on  " + textBody, tk.term().equals("redirect"));
				assertFalse("met thumb on  " + tk + textBody, tk.term().equals("thumb"));
				assertFalse("met camelcas on  " + textBody, tk.term().contains("camelcas"));
				assertFalse("met style on term " + tk + " in "+ textBody, tk.term().equals("style"));
				assertFalse("met background on term " + tk + " in "+ textBody, tk.term().equals("background"));
				assertFalse("met ref on term " + tk + " in "+ textBody, tk.term().equals("ref"));
				assertFalse("met colspan on term " + tk + " in "+ textBody, tk.term().equals("colspan"));
				assertFalse("met font on term " + tk + " in "+ textBody, tk.term().equals("font"));
				assertFalse("met bgcolor on term " + tk + " in "+ textBody, tk.term().equals("bgcolor"));
				assertFalse("met cellspac on term " + tk + " in "+ textBody, tk.term().contains("cellspac"));
				assertFalse("met refer on term " + tk + " in "+ textBody, tk.term().equals("refer"));
				assertFalse("met infobox on term " + tk + " in "+ textBody, tk.term().equals("infobox"));
				assertFalse("met reflist on term " + tk + " in "+ textBody, tk.term().equals("reflist"));
				assertFalse("met ffffff on term " + tk + " in "+ textBody, tk.term().equalsIgnoreCase("ffffff"));
				assertFalse("met defaultsort on term " + tk + " in "+ textBody, tk.term().equalsIgnoreCase("defaultsort"));
				assertFalse("met extern on term " + tk + " in "+ textBody, tk.term().contains("extern"));
				
				assertFalse("met user on term " + tk + " in "+ textBody, tk.term().equals("user"));
				assertFalse("met talk on term " + tk + " in "+ textBody, tk.term().equals("talk"));
				assertFalse("met comment on term " + tk + " in "+ textBody, tk.term().equals("comment"));
//				assertFalse("met date on term " + tk + " in "+ textBody, tk.term().equals("date"));
				assertFalse("met refnum on term " + tk + " in "+ textBody, tk.term().equals("refnum"));
				
				assertFalse(tk.term().matches(NoNumbersFilter.REGEX_STRING));
				assertFalse(tk.term().length() == 1);
				
				System.err.print(tk.term() + " ");
					
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getParagraphCounter() {
		return -1;
	}

	static Logger LOG = Logger.getLogger(AssertingTextProcessor.class);
	
}