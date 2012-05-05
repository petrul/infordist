package store.matrix;

//import inform.dist.MatrixAccessor;

/**
 * store a matrix of integers.
 * @author dadi
 */
public interface IntMatrixStore {
	
	/**
	 * @return the number of rows
	 */
	int getRows();

	/**
	 * 
	 * @return the number of columns
	 */
	int getColumns();
	
	/**
	 * set a cell of the matrix
	 */
	void put(int i, int j, int elem);
	
	/**
	 * get the value of a cell of the matrix
	 */
	int get(int i, int j);
	
	/**
	 * write a whole row at once
	 */
	void putRow(int i, int[] elems);

	/**
	 * get the copy of a whole row (copy means that you can modify the returned values
	 * without modifying the original matrix
	 */
	int[] copyOfRow(int i);
	
	/**
	 * initializas every cell of the matrix to that value
	 */
	void init(int i);

	/**
	 * makes sure changes go the physical underlying device
	 */
	void flush();

	/**
	 * closes the underlying device
	 */
	void close();
	
	/**
	 * put the whole {@link MatrixAccessor} in this 
	 */
	void set(MatrixAccessor<Integer> mtx);

	void reopen();
}
