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
		int arraySize = n * (n - 1) / 2;
		this.array = new int[arraySize];
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
