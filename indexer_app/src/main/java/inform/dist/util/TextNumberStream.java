package inform.dist.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * a stream of numbers, integers or doubles, usable for accessing in a memory
 * efficient a disk-based matrix, for example
 * 
 * @author dadi
 */
public class TextNumberStream implements NumberStream {

	LineNumberReader reader;
	String name;

	Integer rows = null;
	Integer columns = null;

	LinkedList<String> crtTokens = new LinkedList<String>();

	public TextNumberStream(String name, Reader r) {
		this.reader = new LineNumberReader(r);
	}

	public TextNumberStream(String name, Reader reader, int rows, int columns) {
		this.name = name;
		if (reader instanceof LineNumberReader)
			this.reader = (LineNumberReader) reader;
		else
			this.reader = new LineNumberReader(reader);
		this.rows = rows;
		this.columns = columns;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextInt()
	 */
	public Integer nextInt() {
		String string = this.getNextToken();
		if (string == null) return null;
		return Integer.parseInt(string);
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextString()
	 */
	public String nextString() {
		String string = this.getNextToken();
		return string;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextDouble()
	 */
	public Double nextDouble() {
		String string = this.getNextToken();
		if (string == null) return null;
		return Double.parseDouble(string);
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextIntRow()
	 */
	public int[] nextIntRow() {
		if (this.columns == null)
			throw new IllegalAccessError(
					"you haven't provided a columns argument at construction, how do you want me to guess it ? Stupid user...");

		int[] crtIntRow = new int[this.columns];

		for (int i = 0; i < this.columns; i++) {
			Integer nextInt = this.nextInt();
			if (nextInt == null)
				return null;
			crtIntRow[i] = nextInt;
		}
		return crtIntRow;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextDoubleRow()
	 */
	public double[] nextDoubleRow() {
		if (this.columns == null)
			throw new IllegalAccessError(
					"you haven't provided a columns argument at construction, how I am supposed to guess it ? Stupid user...");

		double[] crtDoubleRow = new double[this.columns];

		for (int i = 0; i < this.columns; i++) {
//			LOG.info("col " + i);
			Double nextDouble = this.nextDouble();
			if (nextDouble == null)
				return null;
			crtDoubleRow[i] = nextDouble;
		}

		return crtDoubleRow;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#nextStringRow()
	 */
	public String[] nextStringRow() {
		if (this.columns == null)
			throw new IllegalAccessError(
					"you haven't provided a columns argument at construction, how I am supposed to guess it ? Stupid user...");

		String[] crtRow = new String[this.columns];

		for (int i = 0; i < this.columns; i++) {
			String nextString = this.nextString();
			if (nextString == null)
				return null;
			crtRow[i] = nextString;
		}

		return crtRow;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#getRows()
	 */
	public Integer getRows() {
		return rows;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#setRows(java.lang.Integer)
	 */
	public void setRows(Integer rows) {
		this.rows = rows;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#getColumns()
	 */
	public Integer getColumns() {
		return columns;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#setColumns(java.lang.Integer)
	 */
	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.NumberStream#getName()
	 */
	public String getName() {
		return name;
	}

	private String getNextToken() {
		while (this.crtTokens.size() == 0) {
			try {
				String newLine = this.reader.readLine();
				if (newLine == null)
					return null;
				String[] strings = newLine.split("\\s+");
				for (String s : strings) {
					if ("".equals(s))
						continue;
					this.crtTokens.add(s);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return crtTokens.poll();
	}

	@Override
	public Long nextLong() {
		String string = this.getNextToken();
		if (string == null) return null;
		return Long.parseLong(string);
	}
	static Logger LOG = Logger.getLogger(NumberStream.class);


}
