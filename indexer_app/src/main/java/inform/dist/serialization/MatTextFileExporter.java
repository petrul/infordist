package inform.dist.serialization;

import inform.dist.Constants;
import inform.dist.MatrixAccessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Date;


/**
 * Tool to export a matrix to an Octave file that you can load using the 'load' function 
 */
public class MatTextFileExporter implements MatExporter {
	
	PrintWriter printer;
	
	public MatTextFileExporter(File f, String comment) {
		try {
			this.printer = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			this.init(comment);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MatTextFileExporter(Writer f, String comment) {
		this.printer = new PrintWriter(f);
		this.init(comment);
	}
	
	private void init(String comment) {
		this.printer.println("# Created by " + this.getClass().getCanonicalName() + ", " + new Date());
		this.printer.println("# " + comment);
		this.printer.println();
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.Exporter#writeScalar(java.lang.String, long, java.lang.String)
	 */
	public void writeScalar(String varName, final long value, String comment) {
		MatrixAccessor<Long> one_one_matrix = new MatrixAccessor<Long>() {
			@Override
			public Long getCell(int i, int j) { return value;}
			@Override
			public int getColumns() {return 1; }
			@Override
			public int getRows() { return 1;}
		};
		this.writeMatrix(varName, one_one_matrix, comment);
	}
	
	
	/* (non-Javadoc)
	 * @see inform.dist.util.Exporter#writeMatrix(java.lang.String, inform.dist.MatrixAccessor, java.lang.String)
	 */
	public void writeMatrix(String varName, MatrixAccessor<? extends Object> matrix, String comment) {
		this.writeMatrixHeading(varName, matrix.getRows(), matrix.getColumns(), comment);
		for (int i = 0; i < matrix.getRows(); i++) {
			this.printer.println();
			for (int j = 0; j < matrix.getColumns(); j++) {
				this.printer.print(matrix.getCell(i, j));
				this.printer.print(" ");
			}
		}
	}
	
	private void writeMatrixHeading(String varName, int rows, int columns, String comment) {
		this.printer.print("\n\n#");
		this.printBigDiezBar();
		this.printer.println("# name:" + varName);
		this.printer.println("# type: matrix");
		this.printer.println("# rows:" + rows);
		this.printer.println("# columns:" + columns);
		if (comment != null && ! "".equals(comment.trim())) // only write comment if non-empty
			this.printer.println("# comment:" + comment);
	}

	/* (non-Javadoc)
	 * @see inform.dist.util.Exporter#writeStringArray(java.lang.String, java.lang.String[], java.lang.String)
	 */
	public void writeStringArray(String varName, String[] strings, String comment) {
		try {
			this.printer.print("\n\n#");
			this.printBigDiezBar();
			this.printer.println("# name:" + varName);
			this.printer.println("# type: string-array");
			this.printer.println("# size:" + strings.length);
			this.printer.println("# comment:" + comment);
			for (int i = 0; i < strings.length; i++) {
				this.printer.println();
				this.printer.print(i);
				this.printer.print("\t");
				this.printer.print("\"");
				//this.printer.print(strings[i].replace("\"", "\\\"")); // escape double quotes
				this.printer.print(URLEncoder.encode(strings[i], Constants.UTF8_ENCODING));
				this.printer.print("\"");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void printBigDiezBar() {
		// write a great number of diez's 
		for (int i = 0; i < 80; i++) {
			this.printer.print('#');
		}
		this.printer.println();
	}
	
	public void done() {
		this.printer.close();
	}

	@Override
	public void close() {
		this.done();
	}

	@Override
	public void flush() {
		this.printer.flush();
	}


}
