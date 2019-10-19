package matrix.store;

import inform.dist.MatrixAccessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.commons.collections.map.LRUMap;
import org.junit.Assert;

public class NioFileMatrixStore implements IntMatrixStore {

	protected int 	rows;
	protected int 	columns;
	FileChannel 	channel;

	int BYTES_PER_INT = Integer.SIZE / 8;

	// row cache
	LRUMap 			cache;

	long 			startingPosition;

	String 			openMode;
	
	private File 	file;
	private int 	cacheSize;


	public NioFileMatrixStore(File f, int rows, int columns, String openMode) {
		this(f, rows, columns, 5, openMode);
	}


	/**
	 * @param cacheSize the number of rows to keep available in memory for quick access.
	 *                  should be equal to the number of paralles threads working on this matrix (8 on octoprocessor)
	 */
	public NioFileMatrixStore(File f, int rows, int columns, int cacheSize,
			String openMode) {
		this.rows = rows;
		this.columns = columns;
		this.file = f;
		this.cacheSize = cacheSize;
		this.openMode = openMode;
		
		init();
	}


	/**
	 * 
	 */
	private void init() {
		File f = this.file; // alias
		
		if (f.exists() && !f.isFile())
			throw new IllegalArgumentException("location [" + f + "] is not a file");

		try {
			this.channel = new RandomAccessFile(f, this.openMode).getChannel();
			this.startingPosition = 0;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.cache = new LRUMap(Math.min(cacheSize, rows));
		
	}

	@Override
	public int getColumns() {
		return 0;
	}

	@Override
	public int getRows() {
		return 0;
	}

	@Override
	public void init(int value) {
		int[] row = new int[this.columns];
		for (int i = 0; i < this.columns; i++) {
			row[i] = value;
		}

		for (int i = 0; i < this.rows; i++) {
			this.putRow(i, row);
		}
	}

	@Override
	public void put(int i, int j, int elem) {
		IntBuffer row = this.getRow(i);

		row.put(j, elem);
	}

	@Override
	public void putRow(int i, int[] elems) {
		Assert.assertEquals(this.columns, elems.length);
		long position = this.startingPosition + (long) BYTES_PER_INT * (long) i * (long) this.columns; // 4 bytes per int
		ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES_PER_INT * elems.length);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(elems);
		try {
			this.channel.write(byteBuffer, position);
		} catch (IOException e) {
			throw new RuntimeException("position = " + position, e);
		}
	}

	public int get(int i, int j) {
		return this.getRow(i).get(j);
	}

	/**
	 * return o copy of the data in row i. If you modify an element in the given
	 * array, you DO NOT modify the file too.
	 */
	public int[] copyOfRow(int i) {
		IntBuffer rowbuff = this.getRow(i);
		rowbuff.position(0);
		Assert.assertEquals(this.columns, rowbuff.capacity());
		int[] row = new int[this.columns];
		for (int counter = 0; counter < this.columns; counter++)
			row[counter] = rowbuff.get();
		return row;
	}

	protected IntBuffer getRow(int i) {
		synchronized (this.cache) {
			Assert.assertTrue(i < this.rows);
			if (this.cache.containsKey(i))
				return ((IntBuffer) this.cache.get(i));
			return this.loadRowsIntoMemory(i);
		}
	}

	protected IntBuffer loadRowsIntoMemory(int i) {
		try {
			long position = this.startingPosition + BYTES_PER_INT * i * this.columns;
			long rowsize = BYTES_PER_INT * this.columns;

			MapMode mapmode = this.openMode.equals("r") ?
					FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;
			MappedByteBuffer mbb = this.channel.map(mapmode, position, rowsize);
			IntBuffer result = mbb.asIntBuffer();
			this.cache.put(i, result);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			this.channel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void flush() {
		try {
			this.channel.force(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(MatrixAccessor<Integer> mtx) {
		for (int i = 0; i < mtx.getRows(); i++) {
			for (int j = 0; j < mtx.getColumns(); j++) {
				Integer cell = mtx.getCell(i, j);
				this.put(i, j, cell);
			}
		}
	}
	
	public void reopen() {
		this.init();
	}
}
