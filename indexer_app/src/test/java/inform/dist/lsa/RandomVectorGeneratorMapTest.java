package inform.dist.lsa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * test for {@link RandomVectorGeneratorMap}
 * @author dadi
 *
 */
public class RandomVectorGeneratorMapTest {

	@Test
	public void testGetObject() {
		
		RandomVectorGeneratorMap map = new RandomVectorGeneratorMap(1800, 8);
		int[] vec1 = map.get(1);
		int[] vec2 = map.get(2);
		assertTrue(! vec1.equals(vec2));
		int[] vec1_again = map.get(1);
		assertTrue(vec1.equals(vec1_again));
		
//		IntArrayList 	intArrayList 	= new IntArrayList();
//		ObjectArrayList objectArrayList = new ObjectArrayList();
//		
//		vec1.getNonZeros(intArrayList, objectArrayList);
		int minusOnes = 0;
		int plusOnes = 0;
		
		for (int i = 0; i < vec1.length; i++) {
			if (vec1[i] == -1) minusOnes++;
			if (vec1[i] == +1) plusOnes++;
		}
		
		assertEquals(4, minusOnes);
		assertEquals(4, plusOnes);
	}

	@Test
	public void testItThoroughly() {
		for (int i = 0; i < 10000; i++) {
			this.testGetObject();
		}

	}
	
	Logger LOG = Logger.getLogger(RandomVectorGeneratorMapTest.class);
}
