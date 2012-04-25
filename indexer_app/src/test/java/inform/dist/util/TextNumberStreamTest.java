package inform.dist.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TextNumberStreamTest {

	@Test
	public void nextIntRow() throws Exception {
		String s = "1 2\n3 4 5 6\n7  8\n\t9\t0";
		StringReader stringReader = new StringReader(s);
		NumberStream stream = new TextNumberStream("myvar", stringReader, 5, 2);
		int counter = 0;
		int[] crt;
		while ( (crt = stream.nextIntRow()) != null) {
			assertEquals(2, crt.length);
			counter ++;
		}
		assertEquals(5, counter);
	}

	
	@Test
	public void nextDoubleRow() throws Exception {
		String s = "1.0 .2\n.003 \t 4 5 6\n7  8\n\t9\t0";
		StringReader stringReader = new StringReader(s);
		NumberStream stream = new TextNumberStream("myvar", stringReader, 5, 2);
		int counter = 0;
		double[] crt;
		while ( (crt = stream.nextDoubleRow()) != null) {
			assertEquals(2, crt.length);
			counter ++;
		}
		assertEquals(5, counter);
	}
	
	@Test
	public void testNextInt() {
		String s = "1 2 333 40000 -1";
		String[] numbers = s.split(" ");
		StringReader stringReader = new StringReader(s);
		NumberStream stream = new TextNumberStream("myvar", stringReader);
		int counter = 0;
		Integer crtInt;
		while ( (crtInt = stream.nextInt()) != null) {
			assertEquals(Integer.parseInt(numbers[counter]), (int)crtInt);
			counter ++;
		}
		assertEquals(5, counter);
	}

	@Test
	public void testNextDouble() {
		String s = "1 2.0 .333 .40000 -1.";
		String[] numbers = s.split(" ");
		StringReader stringReader = new StringReader(s);
		NumberStream stream = new TextNumberStream("myvar", stringReader);
		int counter = 0;
		Double crt;
		while ( (crt = stream.nextDouble()) != null) {
			assertEquals(Double.parseDouble(numbers[counter]), crt, .0);
			counter ++;
		}
		assertEquals(5, counter);
	}
	
	Logger LOG = Logger.getLogger(TextNumberStreamTest.class);
}
