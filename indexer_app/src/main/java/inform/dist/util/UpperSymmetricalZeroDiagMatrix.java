package inform.dist.util;

/**
 * this is a symmetric matrix which has zero on the diagonal; only the upper part is 
 * actually stored. The actual memory needed is n * (n - 1) / 2  which is less than
 * two times than the original n * n matrix.
 *  
 * @author dadi
 *
 */
public class UpperSymmetricalZeroDiagMatrix {

	int[] array;
	int n;
	
	public UpperSymmetricalZeroDiagMatrix(int n) {
		this.n = n;
		this.array = new int[computeArraySize(n)];
	}

	/**
	 * n * (n - 1) overflows a 32-bit int for n >= ~46341 (e.g. n=55000 wraps
	 * to a negative product, then the constructor used to fail with
	 * NegativeArraySizeException) -- computed here in long first, only
	 * narrowed back to int once confirmed to still fit in a Java array.
	 * Package-private (rather than folded directly into the constructor) so
	 * it's testable without actually allocating a multi-gigabyte array.
	 */
	static int computeArraySize(int n) {
		long arraySizeLong = (long) n * (n - 1) / 2;
		if (arraySizeLong > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				"n=" + n + " is too large: n*(n-1)/2 = " + arraySizeLong +
				" exceeds the maximum array size (" + Integer.MAX_VALUE + ")");
		}
		return (int) arraySizeLong;
	}
	
	public int get(int x, int y) {
		if (x == y) return 0;
		int index = this.calculateTrueIndex(x,y);
		return this.array[index];
	}

	public void set(int x, int y, int value) {
		if (x == y && value != 0) throw new IllegalArgumentException("diagonal should be zero");
		int index = this.calculateTrueIndex(x, y);
		this.array[index] = value;
	}
	
	private int calculateTrueIndex(int x, int y) {
		int _x, _y;
		if (x > y) {
			_x = y; _y = x;
		} else {
			_x = x;
			_y = y;
		}
		
		int index = 0;
		for (int i = 0; i < _x; i++) {
			index += n - 2 - i;
		}
		index += _y - 1;
		
		return index;
	}

	public int getSize() {
		return this.n;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				sb.append(this.get(i, j)).append(" ; ");
			}
			sb.append("\n");
			
		}
		return sb.toString();
	}
	
}
