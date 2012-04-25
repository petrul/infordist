package inform.dist;

/**
 * use this as a callback when you want to pass a non-RAM matrix to a stream-like processing
 * mechanism. or a matrix that calculates itself at runtime.
 * 
 * @author dadi
 *
 * @param <T> int, double ...
 */
public interface MatrixAccessor<T> {
	
	T getCell(int i, int j);
	
	int getRows();
	
	int getColumns();
	
}
