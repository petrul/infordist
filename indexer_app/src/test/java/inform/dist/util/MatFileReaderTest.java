package inform.dist.util;

import static org.junit.Assert.*;

import inform.dist.MatrixAccessor;
import inform.dist.serialization.MatExporter;
import inform.dist.serialization.MatReader;
import inform.dist.serialization.MatTextFileExporter;
import inform.dist.serialization.MatTextFileReader;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link MatTextFileReader}
 * @author dadi
 *
 */
public class MatFileReaderTest {

	@Test
	public void testGetNumberStream() {
		
		String s = "#created from wikipedia dump [/infres/ir300/ic2/dimulesc/work/infordist/not_versioned/enwiki-latest-pages-articles.xml],term list [/infres/ir300/ic2/dimulesc/work/infordist/not_versioned/terms-sup10.txt]\n" + 
				"# name:total_docs\n" + 
				"# type: matrix\n" +
				"# rows:1\n" + 
				"# columns:1\n" + 
				"42541500\n" + 
				"# name:cooccurrences\n" + 
				"# type: matrix\n" + 
				"# rows:2\n" + 
				"# columns:14\n" + 
				"# comment:a cell is the number of cooccurrences (in a common window of text) of terms i and j; this is a symmetrical matrix with zeroes on the diagonal; source: english wikipedia\n" + 
				"0 620486 423751 616333 1683722 291991 575767 561076 387758 375246 482994 384320 648702 224627\n" +
				"0 620486 423751 616333 1683722 291991 575767 561076 387758 375246 482994 384320 648702 224627";

		{
			MatReader reader = new MatTextFileReader(new StringReader(s));
			NumberStream numberStream = reader.getNumberStream("cooccurrences");
			assertNotNull(numberStream);
			Assert.assertTrue(2 == numberStream.getRows());
			Assert.assertTrue(14 == numberStream.getColumns());
			
			int rowcounter = 0;
			int[] crtrow;
			while ((crtrow = numberStream.nextIntRow()) != null) {
				Assert.assertEquals(14, crtrow.length);
				rowcounter++;
			}
			assertEquals(2, rowcounter);

		}
		
		{
			MatReader reader = new MatTextFileReader(new StringReader(s));
			try {
				reader.getNumberStream("foo");
			} catch (IllegalArgumentException e) {
				//good
			}
		}

		{
			MatTextFileReader reader = new MatTextFileReader(new StringReader(s));
			assertEquals("total_docs", reader.getFirstNumberStream().getName());
		}
	}
	
	
	@Test
	public void testOnRealFile() throws Exception {
		
		Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("sample-mtx-20kcolumns.txt"));
		MatReader matFileReader = new MatTextFileReader(reader);
		NumberStream numberStream = matFileReader.getNumberStream("ungd");
		assertNotNull(numberStream);
		
		double[] nextDoubleRow = numberStream.nextDoubleRow();
		assertNotNull(nextDoubleRow);
		assertEquals(20 * 1000, nextDoubleRow.length);
		
		nextDoubleRow = numberStream.nextDoubleRow();
		assertNotNull(nextDoubleRow);
		assertEquals(20 * 1000, nextDoubleRow.length);
		
		nextDoubleRow = numberStream.nextDoubleRow();
		assertNull(nextDoubleRow);
	}
	
	@Test
	public void exportThenRead() throws Exception {
		final int MATRIXSIZE = 500;
		final int NRSTRINGS = 200;
		
		StringWriter sw = new StringWriter();
		MatExporter matfile = new MatTextFileExporter(sw, "my comment");
		
		
		final String[] strings = new String[NRSTRINGS];
		for (int i = 0; i < NRSTRINGS; i++)
			strings[i] = RandomStringUtils.randomAlphabetic(30);
		
		matfile.writeStringArray("string-array", strings, "my string array");

		matfile.writeMatrix("matrix", new MatrixAccessor<Double>(){

			@Override
			public Double getCell(int i, int j) {
				return new Random().nextDouble();
			}

			@Override
			public int getColumns() {
				return MATRIXSIZE;
			}

			@Override
			public int getRows() {
				return MATRIXSIZE;
			}
			
		}, "matrix comment");
		
		String string = sw.getBuffer().toString();

		MatReader matreader = new MatTextFileReader(new StringReader(string));

		{
			String[] reread_strings = matreader.getStringArray("string-array");
			Assert.assertEquals(NRSTRINGS, reread_strings.length);
			for (int i = 0; i < NRSTRINGS; i++) {
				assertEquals(strings[i], reread_strings[i]);
			}
		}
		
		{
			NumberStream numberStream = matreader.getNumberStream("matrix");
			
			int counter = 0;
			while (counter++ < MATRIXSIZE * MATRIXSIZE) {
				Assert.assertTrue(numberStream.nextDouble() != null);
			}
			
			Assert.assertNull(numberStream.nextDouble());
		}
		
	}

}
