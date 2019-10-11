package wiki.indexer;

import static org.junit.Assert.assertFalse;

import java.io.BufferedInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wiki.indexer.tokenizer.CustomWikipediaTokenizer;

public class WikipediaSnowballAnalyzerTest {

	WikipediaSnowballAnalyzer analyzer = new WikipediaSnowballAnalyzer("English");

	/* real test */
	@Test
	public void testNoUrlsGetThrough() throws Exception {
		String s = "[http://www.google.com|Foaie verde]";
		TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(s));
		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
			// LOG.info("current token " + tk.term() + " , " + tk.type());
		}
	}

	@Test
	public void testTheHeadingsProblem() throws Exception {

		char[] chars = new char[100];
		String s = "\n    {{Anarchism sidebar}}";
		s.getChars(0, s.length(), chars, 0);

		TokenStream tokenStream = analyzer.tokenStream("text", new CharArrayReader(chars, 0, s.length()));

		Token tk = new Token();
		int counter = 0;
		while (tokenStream.next(tk) != null) {
			counter++;
		}
		Assert.assertEquals(0, counter);
	}

	@Test
	public void testStrangeUndocumentedCurlyBraces() throws Exception {
		String s = "'''Anarchism''' is a [[political philosophy]] encompassing theories and attitudes which "
				+ "support the elimination of all compulsory [[government]],"
				+ "<ref name=definitions>*[[Errico Malatesta]], \"[http://www.marxists.org/archive/malatesta/1930s/xx/toanarchy.htm Towards Anarchism]\", ''MAN!''. [[Los Angeles]]: International Group of San Francisco. {{OCLC|3930443}}.\n"
				+ "*{{cite journal |url=http://www.theglobeandmail.com/servlet/story/RTGAM.20070514.wxlanarchist14/BNStory/lifeWork/home/\n"
				+ "|title=Working for The Man |journal=[[The Globe and Mail]] |accessdate=2008-04-14 |last=Agrell |first=Siri |date=[[2007-05-14]]}}";

		TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(s));
		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
			assertFalse(tk.term().equals("http"));
			assertFalse(tk.term().matches("[\\d\\/\\&]"));
			// LOG.info("current token " + tk.term() + " , " + tk.type());
		}
	}

	@Test
	public void testHttpDoesnotPassThrough() throws Exception {
		String s = "==External links==\n"
				+ "*[http://www.eoearth.org/article/Albedo Albedo - Encyclopedia of Earth]\n"
				+ "*[http://lpdaac.usgs.gov/modis/mod43b1.asp NASA MODIS Terra BRDF/albedo product site] \n"
				+ "*[http://www-modis.bu.edu/brdf/product.html NASA MODIS BRDF/albedo product site] \n"
				+ "*[http://www.eumetsat.int/Home/Main/Access_to_Data/Meteosat_Meteorological_Products/Product_List/SP_1125489019643?l=en Surface albedo derived from Meteosat observations]\n"
				+ "*[http://jeff.medkeff.com/astro/lunar/obs_tech/albedo.htm A discussion of Lunar albedos]\n" + "\n"
				+ "{{Global warming}}";

		TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(s));
		Token tk = new Token();

		while (tokenStream.next(tk) != null) {
			// System.out.print(tk.term() + " ");
			Assert.assertFalse("met term " + tk + " containing 'http'", tk.term().toLowerCase().contains("http"));
		}

		/* strangely the succession of heading 2 and heading 3 made the urls at the
		 * end of this paragraph to not be recognized as urls*/
		s = "==Some examples of terrestrial albedo effects==\n\n" +
				"===The tropics===\n" + 
				"Although the albedo-temperature effect is most famous in colder regions of Earth," +
				" because more [[snow]] falls there, it is actually much stronger in tropical " +
				"regions because in the tropics there is consistently more sunlight. " +
				"When ranchers cut down dark, tropical [[rainforest]] trees to " +
				"replace them with even darker soil in order to grow crops, " +
				"the average temperature of the area increases up to 3 °C (5.4 °F) " +
				"year-round,&lt;ref&gt;Dickinson, R. E., and P. J. Kennedy, 1992: " +
				"''Impacts on regional climate of Amazon deforestation''. " +
				"Geophys. Res. Lett., '''19''', 1947–1950.&lt;/ref&gt;&lt;ref&gt;[http://web.mit.edu/12.000/www/m2006/final/characterization/abiotic_water.html http://web.mit.edu/12.000/www/m2006/final/characterization/abiotic_water.html]  Project Amazonia: Characterization - Abiotic - Water&lt;/ref&gt; although part of the effect is due to changed evaporation ([[latent heat]] flux).\n";
		
		tokenStream = analyzer.tokenStream("text", new StringReader(s));
		tk = new Token();

		while (tokenStream.next(tk) != null) {
//			 System.out.println(tk + " ");
			Assert.assertFalse("met term " + tk + " containing 'http'", tk.term().toLowerCase().contains("http"));
		}
	}

	/**
	 * this test takes long.It is a good test and should be run from time to time but it taks too long to execute everytime
	 */
	@Test
	public void testTokenStreamStringReader() throws Exception {
		AssertingTextProcessor processor = new AssertingTextProcessor();
		WikipediaDumpSaxParser saxParser = new WikipediaDumpSaxParser(processor);
		String wikipediaXmlLocation = this.getClass().getClassLoader().getResource("wikipedia-excerpt.xml").getPath();

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(saxParser);
		xmlReader.parse(new InputSource(new BufferedInputStream(new FileInputStream(new File(wikipediaXmlLocation)))));
	}

	static Logger LOG = Logger.getLogger(WikipediaSnowballAnalyzerTest.class);
}
