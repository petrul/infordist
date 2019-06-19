package inform.dist;

public class DistanceCalculator {
	
	/**
	 * @param c1 C(x)
	 * @param c2 C(y)
	 * @param cc combined complexity, C(x,y)
	 */
	static public double getNormalizedDistance(int c1, int c2, int cc) {
		int cmax = Math.max(c1, c2);
		return ((double)getUnnormalizedDistance(c1, c2, cc)) / cmax;
	}
	
	static public int getUnnormalizedDistance(int c1, int c2, int cc) {
		int cmin = Math.min(c1, c2);
		return (cc - cmin);
	}
}
