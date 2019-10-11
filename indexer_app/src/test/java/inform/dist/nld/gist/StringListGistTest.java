package inform.dist.nld.gist;

import static org.junit.Assert.assertEquals;
import inform.dist.Constants;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class StringListGistTest {

	@Test
	public void testCombine() {
		String s1= "a b c\n" 
			+ "b c d\n" +
					"c d e\n";
		String s2 = "x y z\n" +
				"w x y\n";
		
		
		StringListGist g1 = new StringListGist(s1);
		StringListGist g2 = new StringListGist(s2);

//		LOG.info(g1.toString());
//		LOG.info(g2.toString());
		
//		StringListGist both = (StringListGist) g1.clone();
		Gist both = g1.combine(g2);
//		LOG.info(both.toString());
//		LOG.info("asd"  + both.getStringList());
		assertEquals(both.size(), g1.size() + g2.size());

		List<Gist> subGists = both.getSubgists(6);
		assertEquals(3, subGists.size());
		assertEquals(2, subGists.get(0).size());
		assertEquals(2, subGists.get(1).size());
		assertEquals(1, subGists.get(2).size());
	}


	@Test
	public void testWriteTo() {
		String s = "z y x\nc b a";
		StringListGist gist  = new StringListGist(s);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		gist.writeTo(baos);
		String resultingString = new String(baos.toByteArray(), Constants.UTF8_CHARSET);
		//assertEquals("x y z\na b c", resultingString);
		assertEquals(s, resultingString);
	}
	
	static Logger LOG = Logger.getLogger(StringListGistTest.class);
}
