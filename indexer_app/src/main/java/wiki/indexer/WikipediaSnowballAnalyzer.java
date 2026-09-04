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

	private static final String[] ENGLISH_STOPWORDS = new String[] {"s", "i", "a", "about", "an", "are", "as", "at", "be", "by", "com", "de", "en",
			"for", "from", "how", "in", "is", "it", "la", "of", "on", "or", "that", "the",
			"this", "to", "was", "what", "when", "where", "who", "will", "with", "und",
			"www", "and", "not", "br", "ref", "lb"};

	private static final String[] ROMANIAN_STOPWORDS = new String[] {"a", "ai", "al", "ale", "am", "ar", "as", "au", "ca", "care", "ce", "cu", "de", "din",
			"este", "fi", "fie", "fost", "iar", "in", "la", "le", "lor", "mai", "ne", "nu", "o", "pe", "pentru", "prin", "sa", "se", "si", "sunt", "un", "unei", "unor", "unui",
			"ăsta", "aceasta", "acest", "acești", "aceste", "acestea", "acestor", "aceea", "acele", "acelea", "acel", "acela", "acelor", "acei", "aceia",
			"că", "către", "când", "cât", "câte", "câtva", "câți", "dintr", "după", "în", "între", "își", "și", "să", "vă", "voi",
			"com", "www", "br", "ref", "lb"};

	private String name;
	private Set<String> stopSet;
	
	/** Builds the named analyzer with no stop words. */
	@SuppressWarnings("unchecked")
	public WikipediaSnowballAnalyzer(String name) {
		this.name = normalizeLanguage(name);
		this.stopSet = StopFilter.makeStopSet(defaultStopWords(this.name));
	}

	/** Builds the named analyzer with the given stop words. */
	@SuppressWarnings("unchecked")
	public WikipediaSnowballAnalyzer(String name, String[] stopWords) {
		this.name = normalizeLanguage(name);
		stopSet = StopFilter.makeStopSet(stopWords);
	}

	public static String normalizeLanguage(String language) {
		if (language == null)
			throw new IllegalArgumentException("language must not be null");
		if ("en".equalsIgnoreCase(language) || "english".equalsIgnoreCase(language))
			return "English";
		if ("ro".equalsIgnoreCase(language) || "romanian".equalsIgnoreCase(language))
			return "Romanian";
		throw new IllegalArgumentException("unsupported language '" + language + "'; expected English/en or Romanian/ro");
	}

	private static String[] defaultStopWords(String language) {
		return "Romanian".equals(language) ? ROMANIAN_STOPWORDS : ENGLISH_STOPWORDS;
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
