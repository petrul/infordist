package inform.dist;

public class DistanceCalculator {
	
	/**
	 * @param c1 C(x)
	 * @param c2 C(y)
	 * @param cc combined complexity, C(x,y)
	 */
	static public double getNormalizedDistance(long c1, long c2, long cc) {
		long cmax = Math.max(c1, c2);
		return ((double)getUnnormalizedDistance(c1, c2, cc)) / cmax;
	}
	
	static public long getUnnormalizedDistance(long c1, long c2, long cc) {
		long cmin = Math.min(c1, c2);
		return (cc - cmin);
	}
}
