package wiki.indexer;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wiki.indexer.storage.SimpleTermCooccurrenceStorage;


/**
 * test for {@link FrequencyStoringTextProcessor}
 * @author dadi
 *
 */
@Deprecated
public class FrequencyStoringTextProcessorTest {

	@Test
	public void testProcessText() throws Exception {

		Analyzer analyzer = new WikipediaSnowballAnalyzer("English");
		File allowedTerms = new File(this.getClass().getClassLoader().getResource("sup1k.terms").getPath());
		
		SimpleTermCooccurrenceStorage storage = new SimpleTermCooccurrenceStorage(allowedTerms, 200);
		TextProcessor textProcessor = new FrequencyStoringTextProcessor(analyzer, storage);
		
		WikipediaDumpSaxParser saxParser = new WikipediaDumpSaxParser(textProcessor);
		
		InputStream excerpt = this.getClass().getClassLoader().getResourceAsStream("wikipedia-excerpt.xml");
		
		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(saxParser);
		xmlReader.parse(new InputSource(excerpt));

		
		//long nPages = saxParser.getPageCounter();
		long nPages = 10000;
		
		LOG.info("terms : "  + storage.getTermAbsoluteFreq().length);

		
		File file = new File("/tmp/ngd-matrix" + RandomStringUtils.randomAlphabetic(5) + ".csv");
		FileWriter f = new FileWriter(file);
		storage.exportNgdMatrixToCsv(f, nPages);
		f.close();
		file.delete();
		
	}

	Logger LOG = Logger.getLogger(FrequencyStoringTextProcessorTest.class);
}
