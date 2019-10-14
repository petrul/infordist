package wiki.indexer.tokenizer;

import static org.junit.Assert.assertFalse;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.junit.Assert;
import org.junit.Test;

public class CustomWikipediaTokenizerTest {

	/**
	 * test that URLs get excluded (great source of garbage)
	 */
	@Test
	public void testUrlsGetIgnored() throws Exception {
		String s = "'''Anarchism''' is a [[political philosophy]] encompassing theories and attitudes which "
				+ "support the elimination of all compulsory [[government]],"
				+ "<ref name=definitions>*[[Errico Malatesta]], \"[http://www.marxists.org/archive/malatesta/1930s/xx/toanarchy.htm Towards Anarchism]\", ''MAN!''. [[Los Angeles]]: International Group of San Francisco. {{OCLC|3930443}}.\n"
				+ "*{{cite journal |url=http://www.theglobeandmail.com/servlet/story/RTGAM.20070514.wxlanarchist14/BNStory/lifeWork/home/\n"
				+ "|title=Working for The Man |journal=[[The Globe and Mail]] |accessdate=2008-04-14 |last=Agrell |first=Siri |date=[[2007-05-14]]}}";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
			assertFalse(tk.term().equals("http"));
			assertFalse(tk.term().equals("cite"));
			assertFalse(tk.term().equals("journal"));
		}
	}

	@Test
	public void testNoCommentGetsThrough() throws Exception {
		String s = "<!-- NOTES:\n"
				+ "1) Please follow the Wikipedia styyle guidelines for editing medical articles [[WP:MEDMOS]].\n"
				+ "2) Use&lt;ref&gt; for explicitly cited references.\n"
				+ "3) Reference anything you put here with notable references, as this subject tends to attract a lot of controversy.-->";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			Assert.fail("nothing should have passed through");
		}
	}

	@Test
	public void testNoTags() throws Exception {
		String s = "<references/>";
		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			Assert.fail("nothing should have passed through");
		}

		s = "<references>";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			Assert.fail("nothing should have passed through");
		}

		s = "<references />";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			Assert.fail("nothing should have passed through");
		}

	}

	/**
	 * test that table-like styling gets excluded
	 */
	@Test
	public void testStylingAttributesGetIgnored() throws Exception {
		String s = "Seven-bit ASCII provided seven \"national\" characters and, if the combined hardware and software permit, can use overstrikes to simulate some additional international char\n"
				+ "acters: in such a scenario a backspace can precede a [[grave accent]] (which the American and British standards, but only those standards, also call \"opening single quotation \n"
				+ "mark\"), a backtick, or a breath mark (inverted vel).\n"
				+ "\n"
				+ "{| class=\"wikitable\"; style=\"text-align: center; float:left;\"\n"
				+ "|-\n"
				+ "!Binary !! [[Octal|Oct]] !! [[Decimal system|Dec]] !! [[Hexadecimal|Hex]] !! Glyph\n"
				+ "|-\n"
				+ "|010 0000 ||style=\"background:lightblue;\"| 040 ||style=\"background:#CCFFFF;\"| 32 ||style=\"background:lightblue;\"| 20 || [[Space (punctuation)|{{unicod\n"
				+ "e|}}]]\n"
				+ "|-\n"
				+ "|010 0001 ||style=\"background:lightblue;\"| 041 ||style=\"background:#CCFFFF;\"| 33 ||style=\"background:lightblue;\"| 21 || [[Exclamation mark|!]]\n"
				+ "|-\n"
				+ "|010 0010 ||style=\"background:lightblue;\"| 042 ||style=\"background:#CCFFFF;\"| 34 ||style=\"background:lightblue;\"| 22 || [[Quotation mark|\"]]\n"
				+ "|-\n"
				+ "|010 0011 ||style=\"background:lightblue;\"| 043 ||style=\"background:#CCFFFF;\"| 35 ||style=\"background:lightblue;\"| 23 || [[Number sign|#]]\n"
				+ "|";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("style"));
			assertFalse(tk.term().equals("backgound"));

		}

		/* 'bgcolor' */
		s = "===National politics===\n"
				+ "{| align=\"right\" border=\"2\" cellpadding=\"4\" cellspacing=\"0\" style=\"margin: 1em 1em 1em 0; border: 1px #aaa solid; border-collapse: collapse; font-nrLines: 85%;\"\n"
				+ "|+ '''Presidential elections results'''\n" + "|- bgcolor=#D3D3D3\n" + "! Year\n"
				+ "! [[Republican Party (United States)|Republican]]\n"
				+ "! [[Democratic Party (United States)|Democratic]]\n" + "! State winner\n" + "|-\n"
				+ "|align=\"center\" bgcolor=\"#fff3f3\"|[[U.S. presidential election, 2004|2004]]\n"
				+ "|align=\"center\" bgcolor=\"#fff3f3\"|'''62.46%''' ''1,176,394\n"
				+ "|align=\"center\" bgcolor=\"#f0f0ff\"|36.84% ''693,933\n"
				+ "|align=\"center\" bgcolor=\"#fff3f3\"|[[George W. Bush]]";

		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("align"));
			assertFalse(tk.term().equals("bgcolor"));
			assertFalse(tk.term().equals("style"));
			assertFalse(tk.term().equals("backgound"));

		}

		/* jpg with paranthesis */
		s = "[[Image:Academy Award Oscar (cropped).JPG|thumb|widthpx|]]";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equalsIgnoreCase("jpg"));
			assertFalse(tk.term().equalsIgnoreCase("widthpx"));
		}

		/* http */
		s = "*[http://www.time.com/time/archive/collections/0,21428,c_oscars,00.shtml A TIME Archives Collection of the Academy's influence on American Culture]";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equalsIgnoreCase("http"));
		}

		/*
		 * font NB !!! if two tags occur on the same line everything is ignored
		 * in between i don't know how to make flex do ungreedy matching.
		 */
		s = "|HDI_category                = <font color=\"#ffcc00\">medium</font> some text <tagtwo>asd</tagtwo>\nsome text";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		boolean metSomeText = false;
		while (tokenStream.next(tk) != null) {
			if (tk.term().equals("some"))
				metSomeText = true;
			assertFalse(tk.term().equalsIgnoreCase("font"));
			assertFalse(tk.term().equalsIgnoreCase("medium"));
		}
		Assert.assertTrue(metSomeText);

		/* thumb */
		s = "[[Image:RunningtheMachine-LincAdmin.jpg|thumb|right|?Running the ?Machine??: An 1864 political cartoon featuring Lincoln; [[William P. Fessenden|William Fessenden]], [[Edwin M. Sta\n"
				+ "nton|Edwin Stanton]]";

		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("thumb"));

		}

		/* 'style' gets through here */
		s = "The '''albedo''' of an object is the extent to which it diffusely reflects light from the sun.  It is therefore a more specific form of the term [[reflectivity]].  Albedo is defined as\n"
				+ "the ratio of [[diffuse reflection|diffusely reflected]] to incident [[electromagnetic radiation]]. It is a [[Dimensionless number|unitless]] measure indicative of a surface's or body's diffuse [[reflectivity]]. The word is derived from [[Latin]] ''albedo'' \"whiteness\", in turn from ''albus'' \"white\". The range of possible values is from 0 (dark) to 1 (bright).\n"
				+ "[[Image:Albedo-e hg.svg|thumb|Percentage of diffusely reflected sun light in relation to various surface conditions of the earth]]\n"
				+ "\n"
				+ "The albedo is an important concept in [[climatology]] and [[astronomy]]. In climatology it is sometimes expressed as a percentage. Its value depends on the [[frequency]] of radiation considered: unqualified, it usually refers to some appropriate average across the spectrum of [[visible light]]. In general, the albedo depends on the direction and directional distribution of incoming radiation. Exceptions are [[Lambertian]] surfaces, which scatter radiation in all directions in a cosine function, so their albedo does not depend on the incoming distribution. In realistic cases, a [[bidirectional reflectance distribution function]] (BRDF) is required to characterize the scattering properties of a surface accurately, although albedos are a very useful first approximation.\n"
				+ "\n"
				+ "==Terrestrial albedo==\n"
				+ "{| class=\"wikitable\" style=\"float: right;\"\n"
				+ "|+ Sample albedos\n"
				+ "|-\n"
				+ "! Surface\n"
				+ "! Typical<br />Albedo\n"
				+ "|-\n"
				+ "| Fresh asphalt || 0.04<ref name=\"heat island\">{{cite web\n"
				+ " | last=Pon | first=Brian | date=1999-06-30\n"
				+ " | url=http://eetd.lbl.gov/HeatIsland/Pavements/Albedo/\n"
				+ " | title=Pavement Albedo | publisher=Heat Island Group\n"
				+ " | accessdate=2007-08-27\n"
				+ "}}</ref>\n" + "|-";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("style"));
			assertFalse(tk.term().equals("float"));
			assertFalse(tk.term().equals("right"));
			assertFalse(tk.term().equals("wikitable"));
		}

		/* unwanted 'background' */
		s = "{| class=\"wikitable\" style=\"text-align:center;font-nrLines:100%;\"\n"
				+ "| colspan=\"26\" style=\"font-nrLines:120%;background:#E8EAFA;\"|Monthly normal high and low temperatures for various Alabama cities<small><ref>[http://www.ustravelweather.com/weather-ala\n"
				+ "bama/ US Travel Weather]</ref></small>\n" + "|-\n"
				+ "! style=\"background: #D8F8D8; color:#000000\" height=\"17\" colspan=2| Month\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Jan\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Feb\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Mar\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Apr\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| May\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Jun\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Jul\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Aug\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Sep\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Oct\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Nov\n"
				+ "! style=\"background: #D8F8D8; color:#000000;\" colspan=2| Dec\n" + "|-\n"
				+ "! style=\"background: #F8F3CA; color:#000000;\" height=\"17\"| City\n";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("style"));
			assertFalse(tk.term().equals("float"));
			assertFalse(tk.term().equals("right"));
			assertFalse(tk.term().equals("wikitable"));
			assertFalse(tk.term().equals("background"));
			assertFalse(tk.term().equals("font"));
			assertFalse(tk.term().equals("align"));
			assertFalse(tk.term().equals("center"));
			assertFalse(tk.term().equals("color"));
		}

		/* unwanted 'font' */
		s = "===National politics===\n"
				+ "{| align=\"right\" border=\"2\" cellpadding=\"4\" cellspacing=\"0\" style=\"margin: 1em 1em 1em 0; border: 1px #aaa solid; border-collapse: collapse; font-nrLines: 85%;\"\n"
				+ "|+ '''Presidential elections results'''";

		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			assertFalse(tk.term().equals("style"));
			assertFalse(tk.term().equals("float"));
			assertFalse(tk.term().equals("right"));
			assertFalse(tk.term().equals("wikitable"));
			assertFalse(tk.term().equals("background"));
			assertFalse(tk.term().equals("font"));
			assertFalse(tk.term().equals("align"));
			assertFalse(tk.term().equals("center"));
			assertFalse(tk.term().equals("color"));
		}

		/** there was an unwanted pdf */
		s = "{{cite web|url=http://www.mises.org/journals/jls/9_2/9_2_3.pdf|format=pdf|title=Concepts of the role of intellectuals in social change toward laissez faire|author=[[Murray Rothbard]]}}";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		Set<String> terms = new HashSet<String>();
		while (tokenStream.next(tk) != null) {
			terms.add(tk.term());
			assertFalse(tk.term().equals("pdf"));
		}
//		Assert.assertTrue(terms.nrLines() > 10);

		/** there was an unwanted PDFLink */
		s = "\"Anarchism does not exclude prisons, officials, military, or other symbols of force. "
				+ "It merely demands that non-invasive men shall not be made the victims of such force. "
				+ "Anarchism is not the reign of love, but the reign of justice. It does not signify the abolition "
				+ "of force-symbols but the application of force to real invaders.\" Tucker, Benjamin. "
				+ "''Liberty'' 19 October 1891.</ref> and endorsed exchange of labor for wages,<ref>Tucker, "
				+ "Benjamin. \"[http://flag.blackened.net/daver/anarchism/tucker/tucker37.html Labor and "
				+ "Its Pay]\", from ''Individual Liberty: Selections from the Writings of Benjamin T. "
				+ "Tucker''</ref>. They did not have a problem that \"one man employ another\" or that \"he direct "
				+ "him,\" in his labor but demanded that \"all natural opportunities requisite to the production of wealth "
				+ "be accessible to all on equal terms and that monopolies arising from special privileges "
				+ "created by law be abolished.\"<ref>Madison, Charles A. \"Anarchism in the "
				+ "United States\". ''Journal of the History of Ideas'', Vol 6, No 1, January "
				+ "1945, p. 53.</ref> They believed [[state monopoly capitalism]] (defined as a "
				+ "state-sponsored monopoly<ref name=schwartzman>Schwartzman, Jack. \"Ingalls, "
				+ "Hanson, and Tucker: Nineteenth-Century American Anarchists\". ''American Journal of Economics "
				+ "and Sociology'', Vol. 62, No. 5 (November, 2003). p. 325</ref>) prevented labor from "
				+ "being fully rewarded. Even among the nineteenth century American individualists, there was not "
				+ "a monolithic doctrine, as they disagreed amongst each other on various issues including "
				+ "[[intellectual property]] rights and [[Possession (law)|possession]] versus [[property]] in land."
				+ "<ref>Spooner, Lysander. [http://lysanderspooner.org/intellect/contents.htm "
				+ "''The Law of Intellectual Property'']</ref><ref name=watner>"
				+ "[[Carl Watner|Watner, Carl]] (1977). "
				+ "{{PDFlink|[http://www.mises.org/journals/jls/1_4/1_4_4.pdf Benjamin Tucker and His Periodical, "
				+ "Liberty]|868&amp;nbsp;[[Kibibyte|KiB]]<!-- application/pdf, 889419 bytes -->}}. "
				+ "''[[Journal of Libertarian Studies]]'', Vol. 1, No. 4, p. 308</ref><ref>[[Carl Watner|Watner, "
				+ "Carl]]. \"{{PDFlink|[http://www.mises.org/journals/lf/1975/1975_03.pdf Spooner "
				+ "Vs. Liberty]\"|1.20&amp;nbsp;[[Mebibyte|MiB]]<!-- application/pdf, 1262532 bytes -->}} "
				+ "in ''The Libertarian Forum''. March 1975. Volume VII, No 3. ISSN 0047–4517. "
				+ "pp. 5–6.</ref> A major cleft occurred later in the 19th century when Tucker and some others "
				+ "abandoned [[natural right]]s and converted to an \"egoism\" modeled upon [[Philosophy of Max Stirner|Stirner's"
				+ " philosophy]].<ref name=watner/> Some \"Boston anarchists\", like Tucker, identified "
				+ "themselves as \"socialist\" – a term which at the time denoted a broad concept – by which "
				+ "he meant a commitment to solving \"the labor problem\" by radical economic reform.<"
				+ "ref>Brooks, Frank H. 1994. ''The Individualist Anarchists: An Anthology of Liberty'' "
				+ "(1881–1908). Transaction Publishers. p. 75.</ref>) "
				+ "By the turn of the 20th century, the heyday of individualist anarchism had passed,"
				+ "<ref>[[Paul Avrich|Avrich, Paul]]. 2006. ''Anarchist Voices: An Oral History of Anarchism in America''"
				+ ". [[AK Press]]. p. 6</ref> although it was later revived with modifications by "
				+ "[[Murray Rothbard]] and the [[anarcho-capitalists]] in the mid-twentieth century, as a current of "
				+ "the broader [[libertarianism|libertarian]] movement, <ref name=encarta7>Levy, Carl. \""
				+ "[http://uk.encarta.msn.com/encyclopedia_761568770_1/Anarchism.html Anarchism]\". Microsoft [[Encarta]] "
				+ "Online Encyclopedia 2007.</ref><ref>Miller, David. \"Anarchism\". ''The Blackwell Encyclopaedia of Political Thought"
				+ "'' 1987. p. 11</ref> and the anti-capitalist strain by intellectuals such as [[Kevin Carson]].";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		terms = new HashSet<String>();
		while (tokenStream.next(tk) != null) {
			// LOG.info(tk);
			terms.add(tk.term());
			assertFalse(tk.term().toLowerCase().contains("pdf"));
		}
		Assert.assertTrue(terms.size() > 10);

		/* url was not properly recognized */
		s = "url=http://ruccs.rutgers.edu/~aleslie/Baron-Cohen%20Leslie%20&%20Frith%201985.pdf ";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			// LOG.info(tk);
			assertFalse(tk.term().toLowerCase().contains("pdf")
					&& !tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
		}

		/* yet another pdf */
		// s =
		// "in the state.<ref>{{cite web|url=http://www.myaea.org/PDFfile/Confidence%20in%20State%20Institutions07.pdf |title=Confidence in State and Local Institutions Survey";
		s = "===Religion=== \n"
				+ "Alabama is located in the middle of the [[Bible Belt]]. In a 2007 survey, nearly 70% of respondents could name all four of the Christian Gospels. Of those who indicated a religious preference, 59% said they possessed a \"full understanding\" of their faith and needed no further learning.<ref>{{cite news|first=Kirsten |last=Campbell |work=Mobile Register |title=Alabama rates well in biblical literacy |date=2007-03-25 |accessdate=2007-06-02 |page=A1 |publisher=Advance Publications, Inc.}}</ref>  In a 2007 poll, 92% of Alabamians reported having at least some confidence in churches in the state.<ref>{{cite web|url=http://www.myaea.org/PDFfile/Confidence%20in%20State%20Institutions07.pdf |title=Confidence in State and Local Institutions Survey |work=Capital Survey Research Center |accessdate 2007-006-02 "
				+ "|type=PDF |format=PDF}}</ref><ref>{{cite news |first=David |last=White |title=Poll says we feel good about state Trust in government, unlike some institutions, hasn't fallen |date=2007-04-01 |accessdate=2007-06-02 |work=Birmingham News |publisher=Birmingham News |page=13A}}</ref> The Mobile area is notable for its large percentage of Catholics, owing to the area's unique early history under French and Spanish rule. Today, a huge percentage of Alabamians identify themselves as Protestants. The top two largest denominations in the state are the Baptists (40%), Methodists (10%).\n"
				+ "";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			// LOG.info(tk);
			assertFalse(tk.term().toLowerCase().contains("pdf")
					&& !tk.type().equals(CustomWikipediaTokenizer.EXTERNAL_LINK_URL));
		}
	}

	@Test
	public void testHeadingsGetTokenized() throws Exception {
		char[] chars = new char[100];
		String s = "\n    {{Anarchism sidebar}}";
		s.getChars(0, s.length(), chars, 0);

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new CharArrayReader(chars, 0, s.length()));

		Token tk = new Token();
		int counter = 0;
		while (tokenStream.next(tk) != null) {
			// LOG.info(tk.term());
			counter++;
		}
//		Assert.assertEquals(2, counter);
	}

	@Test
	public void testRedirectionGetsIgnored() throws Exception {
		String s = "#REDIRECT [[American Samoa]]{{R from CamelCase}}";
		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			Assert.fail("no token should have passed through, least of all : " + tk);
		}

		// this one has a space between [[]] and {{ }}
		s = "#REDIRECT [[Applied ethics]] {{R from CamelCase}}";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		while (tokenStream.next(tk) != null) {
			Assert.fail("no token should have passed through, least of all : " + tk);
		}

		s = "#redirect [[Argument form]]{{R from CamelCase}}";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		while (tokenStream.next(tk) != null) {
			Assert.fail("no token should have passed through, least of all : " + tk);
		}
	}

	/**
	 * at some point a url (the fourth in the list below) is parsed word by
	 * word, instead of retrieving it as a whole url.
	 */
	@Test
	public void testNoHttp() throws Exception {
		String s = "==External links==\n"
				+ "*[http://www.eoearth.org/article/Albedo Albedo - Encyclopedia of Earth]\n"
				+ "*[http://lpdaac.usgs.gov/modis/mod43b1.asp NASA MODIS Terra BRDF/albedo product site] \n"
				+ "*[http://www-modis.bu.edu/brdf/product.html NASA MODIS BRDF/albedo product site] \n"
				+ "*[http://www.eumetsat.int/Home/Main/Access_to_Data/Meteosat_Meteorological_Products/Product_List/SP_1125489019643?l=en Surface albedo derived from Meteosat observations]\n"
				+ "*[http://jeff.medkeff.com/astro/lunar/obs_tech/albedo.htm A discussion of Lunar albedos]\n" + "\n"
				+ "{{Global warming}}";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		Token tk = new Token();

		while (tokenStream.next(tk) != null) {
			// System.out.println(tk);
			Assert.assertFalse("http".equals(tk.term()));
		}
	}

	@Test
	public void testNoHttp2() throws Exception {
		String s = "AMPAS, a professional honorary organization, maintains a voting membership of 5,829 as "
				+ "of 2007.<ref > {{cite news | author=Sandy Cohen | title=Academy Sets Oscars Contingency Plan | "
				+ "url=http://news.aol.com/entertainment/story/_a/oscars-contingency-plan/20080130161309990001 | work=AOL News | "
				+ "date=2008-01-30 | accessdate=2008-03-19}}</ref >";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		Token tk = new Token();

		while (tokenStream.next(tk) != null) {

			Assert.assertFalse("http".equals(tk.term()));
			Assert.assertFalse("term " + tk + " contains &lt", tk.term().toLowerCase().contains("&lt"));
			Assert.assertFalse("term " + tk + " contains &gt", tk.term().toLowerCase().contains("&gt"));
		}

		s = "==Some examples of terrestrial albedo effects==\n\n"
				+ "===The tropics===\n"
				+ "Although the albedo-temperature effect is most famous in colder regions of Earth,"
				+ " because more [[snow]] falls there, it is actually much stronger in tropical "
				+ "regions because in the tropics there is consistently more sunlight. "
				+ "When ranchers cut down dark, tropical [[rainforest]] trees to "
				+ "replace them with even darker soil in order to grow crops, "
				+ "the average temperature of the area increases up to 3 °C (5.4 °F) "
				+ "year-round,<ref>Dickinson, R. E., and P. J. Kennedy, 1992: "
				+ "''Impacts on regional climate of Amazon deforestation''. "
				+ "Geophys. Res. Lett., '''19''', 1947–1950.</ref><ref>[http://web.mit.edu/12.000/www/m2006/final/characterization/abiotic_water.html http://web.mit.edu/12.000/www/m2006/final/characterization/abiotic_water.html]  Project Amazonia: Characterization - Abiotic - Water</ref> although part of the effect is due to changed evaporation ([[latent heat]] flux).\n";

		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		tk = new Token();

		while (tokenStream.next(tk) != null) {
			Assert.assertFalse(tk.term().equals("http"));
		}

		// ///////////
		s = "{{Infobox award\n" + "| name        = Academy Award\n" + "| image       = Academy Award Oscar.jpg\n"
				+ "| current_awards =\n" + "| description = Excellence in [[film|cinema]]tic achievements\n"
				+ "| presenter   = [[Academy of Motion Picture Arts and Sciences]]\n"
				+ "| country     = [[United States]]\n" + "| year        = May 16, 1929\n"
				+ "| website     = http://www.oscars.org/\n" + "}}";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		tk = new Token();

		while (tokenStream.next(tk) != null) {
			// LOG.info(tk.term() + tk.type());
			Assert.assertFalse(tk.term().equalsIgnoreCase("http"));
		}
	}

	@Test
	public void testInsideImage() throws Exception {
		String s = "\n"
				+ "[[Image:WilliamGodwin.jpg|thumb|left|[[William Godwin]], "
				+ "usually considered an individualist anarchist, is often regarded as producing the first philosophical expression of anarchism]]";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		Token tk = new Token();

		String expectedterms = "William Godwin usually considered an individualist anarchist is often regarded as producing the first philosophical expression of anarchism";
		Set<String> terms = new HashSet<String>();
		while (tokenStream.next(tk) != null) {
			// System.out.print(tk.term() + " ");
			terms.add(tk.term());
			Assert.assertFalse("jpg".equals(tk.term()));
			Assert.assertFalse("WilliamGodwin".equals(tk.term()));
			Assert.assertFalse("thumb".equals(tk.term()));
			Assert.assertFalse("left".equals(tk.term()));
		}

		for (String ext : expectedterms.split(" ")) {
			Assert.assertTrue(terms.contains(ext));
		}
	}

	@Test
	public void testInfoboxDisease() throws Exception {
		String s = "{{Infobox_Disease\n" + " | Name = Autism\n" + " | Image = Autism-stacking-cans 2nd edit.jpg\n"
				+ " | Caption = Repetitively stacking or lining up objects may indicate autism.<ref name=Johnson/>\n"
				+ " | DiseasesDB = 1142\n" + " | ICD10 = {{ICD10|F|84|0|f|80}}\n" + " | ICD9 = 299.0\n" + " | ICDO =\n"
				+ " | OMIM = 209850\n" + " | MedlinePlus = 001526\n" + " | eMedicineSubj = med\n"
				+ " | eMedicineTopic = 3202\n" + " | eMedicine_mult = {{eMedicine2|ped|180}}\n"
				+ " | MeshID = D001321\n" + "}} some text after";

		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
		Token tk = new Token();

		// String expectedterms = "";
		Set<String> terms = new HashSet<String>();
		while (tokenStream.next(tk) != null) {
			// System.out.println(tk + " ");
			terms.add(tk.term());
			Assert.assertFalse("jpg".equals(tk.term()));
			Assert.assertFalse("thumb".equals(tk.term()));
			Assert.assertFalse("left".equals(tk.term()));
			Assert.assertFalse("lt".equals(tk.term()));
		}

		/* i got a 'jpg' here */
		s = "{{Infobox President\n"
				+ "|name = Abraham Lincoln\n"
				+ "|nationality = American\n"
				+ "|image = Abraham Lincoln head on shoulders photo portrait.jpg\n"
				+ "|order = 16th [[President of the United States]]\n"
				+ "|term_start = March 4, 1861\n"
				+ "|term_end = April 15, 1865\n"
				+ "|predecessor = [[James Buchanan]]\n"
				+ "|successor = [[Andrew Johnson]]\n"
				+ "|state2 = [[Illinois]]\n"
				+ "|district2 = [[Illinois' 7th congressional district|7th]]\n"
				+ "|term_start2 = March 4, 1847\n"
				+ "|term_end2 = March 3, 1849\n"
				+ "|predecessor2 = [[John Henry (representative)|John Henry]]\n"
				+ "|successor2 = [[Thomas L. Harris]]\n"
				+ "|birth_date = {{birth date|mf=yes|1809|2|12|mf=y}}\n"
				+ "|birth_place =[[Hardin County, Kentucky]]\n"
				+ "|death_date = {{death date and age|mf=yes|1865|4|15|1809|2|12}}\n"
				+ "|death_place =[[Washington, D.C.]]\n"
				+ "|spouse = [[Mary Todd Lincoln]]\n"
				+ "|children = [[Robert Todd Lincoln]], [[Edward Baker Lincoln|Edward Lincoln]], [[William Wallace Lincoln|Willie Lincoln]], [[Tad Lincoln]]\n"
				+ "|occupation=[[Lawyer]]\n"
				+ "|religion = See: [[Abraham Lincoln and religion]]\n"
				+ "|party = [[Whig Party (United States)|Whig]] (1832-1854), [[History of the United States Republican Party|Republican]] (1854-1864), [[National Union Party (United States)|National \n"
				+ "Union]] (1864-1865)\n"
				+ "|vicepresident = [[Hannibal Hamlin]] (1861 ? 1865)<br />[[Andrew Johnson]] (1865)\n"
				+ "|signature = Abraham Lincoln Signature.png\n" + "}}\n" + "";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		// String expectedterms = "";
		while (tokenStream.next(tk) != null) {
			// LOG.info(tk);
			Assert.assertFalse("jpg".equals(tk.term()));
			Assert.assertFalse("thumb".equals(tk.term()));
			Assert.assertFalse("left".equals(tk.term()));
			Assert.assertFalse("lt".equals(tk.term()));
		}
	}

	@Test
	public void testNonWordsAreStopped() throws IOException {
		// reflist
		String s = "{{reflist|colwidth=30em}}";
		CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		Token tk = new Token();
		while (tokenStream.next(tk) != null) {
			Assert.assertFalse("reflist".equals(tk.term()));
		}

		// infobox
		s = "{{Infobox U.S. state\n" + "|Name          = Alabama";

		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		tk = new Token();
		while (tokenStream.next(tk) != null) {
			Assert.assertFalse("infobox".equalsIgnoreCase(tk.term()));
		}

		s = "{{Reflist|2}}";
		tokenStream = new CustomWikipediaTokenizer(new StringReader(s));

		tk = new Token();
		while (tokenStream.next(tk) != null) {
			LOG.info(tk);
			Assert.assertFalse("reflist".equalsIgnoreCase(tk.term()));
		}

	}

	@Test
	public void testNoExternalLinks() throws IOException {
		{
			String s = "==External links==";
			CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
	
			Token tk = new Token();
			while (tokenStream.next(tk) != null) {
				Assert.fail();
			}
		}		
		
		{
			String s = "==   external Link ==";
			CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
	
			Token tk = new Token();
			while (tokenStream.next(tk) != null) {
				Assert.fail();
			}
		}
		
		{
			String s = "==External links==\n" + 
			"<!--==========================({{NoMoreLinks}})============================\n" + 
			"    | PLEASE BE CAUTIOUS IN ADDING MORE LINKS TO THIS ARTICLE. WIKIPEDIA  |\n" + 
			"    | IS NOT A COLLECTION OF LINKS NOR SHOULD IT BE USED FOR ADVERTISING. |\n" + 
			"    |                                                                     |\n" + 
			"    |           Excessive or inappropriate links WILL BE DELETED.         |\n" + 
			"    | See [[Wikipedia:External links]] &amp; [[Wikipedia:Spam]] for details.  |\n" + 
			"    |                                                                     |\n" + 
			"    | If there are already plentiful links, please propose additions or   |\n" + 
			"    | replacements on this article's discussion page, or submit your link |\n" + 
			"    | to the relevant category at the Open Directory Project (dmoz.org)   |\n" + 
			"    | and link back to that category using the {{dmoz}} template.         |\n" + 
			"    =========================({{NoMoreLinks}})=============================-->";
			CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
	
			Token tk = new Token();
			while (tokenStream.next(tk) != null) {
//				LOG.info(tk);
				Assert.fail("nothing should pass through");
			}
		}
		
		{
			String s = "{{s-start}}\n" + 
					"{{s-par|us-hs}}\n" + 
					"{{s-bef|before=[[John Henry (representative)|John Henry]]}}\n" + 
					"{{s-ttl|title=Member from [[Illinois's 7th congressional district|Illinois's&lt;br /&gt;7th congressional district]]|years=March 4, 1847 – March 3, 1849}}\n" + 
					"{{s-aft|after=[[Thomas L. Harris]]}}\n" + 
					"{{s-off}}\n" + 
					"{{s-bef|before=[[James Buchanan]]}}\n" + 
					"{{s-ttl|title=[[President of the United States]]|years=March 4, 1861 – April 15, 1865}}\n" + 
					"{{s-aft|after=[[Andrew Johnson]]}}\n" + 
					"{{s-ppo}}\n" + 
					"{{s-bef|before=[[John C. Frémont]]}}\n" + 
					"{{s-ttl|title=[[List of United States Republican Party presidential tickets|Republican Party presidential candidate]]|years=[[U.S. presidential election, 1860|1860]], [[U.S. presidential election, 1864|1864]]}}\n" + 
					"{{s-aft|after=[[Ulysses S. Grant]]}}\n" + 
					"{{s-hon}}\n" + 
					"{{s-bef|before=[[Henry Clay]]}}\n" + 
					"{{s-ttl|title=Persons who have [[lying in state|lain in state or honor]]&lt;br /&gt;in the [[United States Capitol rotunda]]|years=April 19, 1865 – April 21, 1865}}\n" + 
					"{{s-aft|after=[[Thaddeus Stevens]]}}\n" + 
					"{{end}}\n" + 
					"{{AbrahamLincoln}}\n" + 
					"{{US Presidents}}\n" + 
					"{{USRepPresNominees}}\n" + 
					"{{Lincoln cabinet}}\n" + 
					"{{Black Hawk War (1832)}}\n" + 
					"\n" + 
					"{{Persondata\n" + 
					"|NAME = Lincoln, Abraham\n" + 
					"|ALTERNATIVE NAMES =\n" + 
					"|SHORT DESCRIPTION = 16th President of the United States of America\n" + 
					"|DATE OF BIRTH = February 12, 1809\n" + 
					"|PLACE OF BIRTH = Hardin County, Kentucky\n" + 
					"|DATE OF DEATH = April 15, 1865\n" + 
					"|PLACE OF DEATH = [[Washington, D.C.]]\n" + 
					"}}\n" + 
					"{{DEFAULTSORT:Lincoln, Abraham}}\n" + 
					"[[Category:1809 births]]\n" + 
					"[[Category:1865 deaths]]\n" + 
					"[[Category:Abraham Lincoln|Abraham Lincoln]]";
			CustomWikipediaTokenizer tokenStream = new CustomWikipediaTokenizer(new StringReader(s));
	
			Token tk = new Token();
			while (tokenStream.next(tk) != null) {
				LOG.info(tk);
				Assert.fail("nothing should pass through");
			}
			
		}
	}

	static Logger LOG = Logger.getLogger(CustomWikipediaTokenizerTest.class);
}
