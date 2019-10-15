package inform.dist.ncd.gist;

import static org.junit.Assert.assertEquals;
import inform.dist.Constants;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.junit.Test;

public class StringGistTest {

	@Test
	public void testCombine() {
		String s1= "a b c\n" 
			+ "b c d\n" +
					"c d e\n";
		String s2 = "x y z\n" +
				"w x y\n";
		
		
		StringGist g1 = new StringGist(s1);
		assertEquals(3, g1.nrLines());

		StringGist g2 = new StringGist(s2);
		assertEquals(2, g2.nrLines());

		Gist both = g1.combine(g2);
		assertEquals(both.nrLines(), g1.nrLines() + g2.nrLines());

//		List<Gist> subGists = both.getSubgists(6);
//		assertEquals(3, subGists.size());
//		assertEquals(2, subGists.get(0).nrLines());
//		assertEquals(2, subGists.get(1).nrLines());
//		assertEquals(1, subGists.get(2).nrLines());
	}


	@Test
	public void testWriteTo() {
		String s = "z y x\nc b a";
		StringGist gist  = new StringGist(s);
		assert gist.nrLines() == 2;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		gist.writeTo(baos);
		String resultingString = new String(baos.toByteArray(), Constants.UTF8_CHARSET);
		//assertEquals("x y z\na b c", resultingString);
		assertEquals(s, resultingString);
	}
	
	static Logger LOG = Logger.getLogger(StringGistTest.class);
}
