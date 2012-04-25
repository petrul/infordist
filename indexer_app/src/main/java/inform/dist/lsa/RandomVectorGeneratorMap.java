package inform.dist.lsa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * this is a map that stores random vectors. If you ask it for a key that has no
 * corresponding vector, it will generate one secretly and return it. You can
 * use it as a mechanism for getting the same random vector for a given key. 
 * 
 * @author dadi
 */
public class RandomVectorGeneratorMap extends HashMap<Object, int[]> {

	private static final long serialVersionUID = 1L;
	private int vectorSize;
	private int nonZeroEntries;

	public RandomVectorGeneratorMap(int vectorSize, int nonZeroEntries) {
		this.vectorSize = vectorSize;
		if (nonZeroEntries % 2 != 0)
			throw new IllegalArgumentException(
					"the non zero entries number in a vector must be even so we can put half negatives and half positives");
		this.nonZeroEntries = nonZeroEntries;

	}

	@Override
	public int[] get(Object key) {
		if (!this.containsKey(key)) {
			int[] vector = this.generateRandomVector();
			this.put(key, vector);
		}
		return super.get(key);
	}

	public int[] generateRandomVector() {
		// ObjectMatrix1D vec = ObjectFactory1D.sparse.make(this.vectorSize, 0);
		int[] vec = new int[this.vectorSize];
		Random random = new Random();

		Set<Integer> positions = new HashSet<Integer>();
		while (positions.size() < this.nonZeroEntries) {
			positions.add(random.nextInt(this.vectorSize));
		}

		int counter = 0;
		for (Integer pos : positions) {
			if (counter % 2 == 0)
				vec[pos] = +1;
			else
				vec[pos] = -1;
			counter++;
		}
		return vec;
	}
}
