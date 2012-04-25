package inform.dist.util;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

/**
 * comes from 
 * @author dadi
 */
public class BinaryNumberStream implements NumberStream {

	IndexInput input;
	int cols;
	int rows;
	private String name;
	
	
	public BinaryNumberStream(String name, int rows, int cols, IndexInput input) {
		this.rows = rows;
		this.cols = cols;
		this.name = name;
		this.input = input;
	}

	@Override
	public Integer getColumns() {
		return this.cols;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Integer getRows() {
		return this.rows;
	}

	@Override
	public Double nextDouble() {
		try {
			long l = this.input.readLong();
			return Double.longBitsToDouble(l);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double[] nextDoubleRow() {
		double[] resp = new double[this.cols];
		for (int i = 0; i < this.cols;i++) {
			resp[i] = this.nextDouble();
		}
		return resp;
	}

	@Override
	public Integer nextInt() {
		try {
			int n = this.input.readInt();
			return n;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] nextIntRow() {
		int[] resp = new int[this.cols];
		for (int i = 0; i < this.cols;i++) {
			resp[i] = this.nextInt();
		}
		return resp;
	}

	@Override
	public String nextString() {
		try {
			String s = this.input.readString();
			return s;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String[] nextStringRow() {
		String[] resp = new String[this.cols];
		for (int i = 0; i < this.cols;i++) {
			resp[i] = this.nextString();
		}
		return resp;
	}

	@Override
	public Long nextLong() {
		try {
			long l = this.input.readLong();
			return l;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}
