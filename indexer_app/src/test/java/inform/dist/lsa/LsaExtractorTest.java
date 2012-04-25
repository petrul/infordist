package inform.dist.lsa;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class LsaExtractorTest {

	@Test
	public void testCosine() {
		
		for (int i = 0; i < 50; i++) {
			long x[] = this.getRandomVector();
			long y[] = this.getRandomVector();
			double dist = LsaExtractor.cosine(x, y);
//			System.out.print("" + dist +", ");
			assertTrue("got distance " + dist, dist >= -1);
			assertTrue("got distance " + dist, dist <= 1);
			
			assertEquals(1.0, LsaExtractor.cosine(x,x), .00000001);
//			System.out.println();
		}
	}

	private long[] getRandomVector() {
		Random rnd = new Random();
		final int VECSIZE = 1800;
		long[] result = new long[VECSIZE];
		for (int i = 0; i < VECSIZE; i++)
			result[i] = rnd.nextInt(500 * 1000);
		return result;
	}

}
