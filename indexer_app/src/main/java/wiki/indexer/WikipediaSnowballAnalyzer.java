package wiki.indexer;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;

import wiki.indexer.filters.NoNumbersFilter;
import wiki.indexer.filters.NoUrlFilter;
import wiki.indexer.tokenizer.CustomWikipediaTokenizer;

public class WikipediaSnowballAnalyzer extends Analyzer {

	private String name;
	private Set<String> stopSet;

	String[] stopwords = new String[] {"s", "I", "a", "about", "an", "are", "as", "at", "be", "by", "com", "de", "en",
			"for", "from", "how", "in", "is", "it", "la", "of", "on", "or", "that", "the",
			"this", "to", "was", "what", "when", "where", "who", "will", "with", "und",
			"the", "www", "and", "not", "br", "ref", "lb"};
	
	/** Builds the named analyzer with no stop words. */
	@SuppressWarnings("unchecked")
	public WikipediaSnowballAnalyzer(String name) {
		this.name = name;
		this.stopSet = StopFilter.makeStopSet(stopwords);
	}

	/** Builds the named analyzer with the given stop words. */
	@SuppressWarnings("unchecked")
	public WikipediaSnowballAnalyzer(String name, String[] stopWords) {
		this(name);
		stopSet = StopFilter.makeStopSet(stopWords);
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new CustomWikipediaTokenizer(reader);
		result = new NoUrlFilter(result); // we do not want to index URLs
		result = new NoNumbersFilter(result); // we do not want to index numbers
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		if (stopSet != null)
			result = new StopFilter(result, stopSet);
		result = new SnowballFilter(result, name);
		
		return result;
	}
}
