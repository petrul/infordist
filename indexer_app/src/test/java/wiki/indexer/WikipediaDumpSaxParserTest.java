package wiki.indexer;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wiki.indexer.storage.SimpleTermCooccurrenceStorage;


public class WikipediaDumpSaxParserTest {

	@Test
	public void analyzeWikiText() throws Exception {
		Analyzer analyzer = new WikipediaSnowballAnalyzer("English");
		InputStream excerpt = this.getClass().getClassLoader().getResourceAsStream("wikipedia-excerpt.xml");
		
//		LOG.info("analyzer " + analyzer);
		Token tk = new Token();
		TokenStream tokenStream = analyzer.tokenStream("text", new InputStreamReader(excerpt));
		while (tokenStream.next(tk) != null) {
//			LOG.info(tk.term() + " - " + tk.type());
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testTextIsProperlySplitInParagrahs() throws Exception {
		String s = "<page><text>a\n\nb\n\nc</text></page>";
		Analyzer analyzer = new StandardAnalyzer();
		
		File allowedTerms = new File(this.getClass().getClassLoader().getResource("sup1k.terms").getPath());
		SimpleTermCooccurrenceStorage storage = new SimpleTermCooccurrenceStorage(allowedTerms, 200);
		FrequencyStoringTextProcessor processor = new FrequencyStoringTextProcessor(analyzer, storage);
		WikipediaDumpSaxParser parser = new WikipediaDumpSaxParser(processor);
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(parser);
		xmlReader.parse(new InputSource(new StringReader(s)));
		
		Assert.assertEquals(3, parser.getTextWindowCounter());
	}

	Logger LOG = Logger.getLogger(WikipediaDumpSaxParserTest.class);
}
