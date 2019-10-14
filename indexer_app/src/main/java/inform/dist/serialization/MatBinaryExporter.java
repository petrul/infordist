package inform.dist.serialization;

import inform.dist.MatrixAccessor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.MMapDirectory;

/**
 * a serializer that uses Lucene's {@link MMapDirectory} in order to write to
 * disk.
 * 
 * @author dadi
 * 
 * @param <T>
 *            the type of the matrix to write.
 */
public class MatBinaryExporter implements MatExporter {

	private IndexOutput output;
	
	
	
	public MatBinaryExporter(String fileName, String comment) {
		try {
			this.output = new MMapDirectory().createOutput(fileName);
			this.writeHeader(comment);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MatBinaryExporter(File file, String comment) {
		try {
			this.output = new MMapDirectory().createOutput(file.getAbsolutePath());
			this.writeHeader(comment);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeHeader(String comment) throws IOException {
		this.output.writeString("matrixdata");
		this.output.writeString(comment);
	}
	
	
	private void writeDataHeader(Map<String, String> map) {
		try {
			this.output.writeString(BinFormatConstants.DATA_HEADER);
			this.output.writeInt(map.size());
			for (String key : map.keySet()) {
				this.output.writeString(key);
				this.output.writeString(map.get(key));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeMatrix(String varName, MatrixAccessor<? extends Object> matrix, String comment) {
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("name", varName);
		header.put("type", "matrix");
		header.put("rows", "" + matrix.getRows());
		header.put("columns", "" + matrix.getColumns());
		header.put("comment", comment);

		this.writeDataHeader(header);

		for (int i = 0; i < matrix.getRows(); i++) {
			for (int j = 0; j < matrix.getColumns(); j++) {
				this.writeObject(matrix.getCell(i, j));
			}
		}
	}

	private void writeObject(Object cell)  {
		try {
			if (cell instanceof Double) {
				double d = (Double) cell;
				long l = Double.doubleToRawLongBits(d);
				output.writeLong(l);
			} else if (cell instanceof Integer) {
				int d = (Integer) cell;
				output.writeInt(d);
			} else if (cell instanceof Long) {
				long l = (Long) cell;
				output.writeLong(l);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}

	@Override
	public void writeScalar(String varName, final long value, String comment) {
		MatrixAccessor<? extends Long> mtx = new MatrixAccessor<Long>() {
			@Override
			public Long getCell(int i, int j) {
				return value;
			}
			@Override
			public int getColumns() {
				return 1;
			}
			@Override
			public int getRows() {
				return 1;
			}
			
		};
		this.writeMatrix(varName, mtx, comment);
	}

	@Override
	public void writeStringArray(String varName, String[] strings, String comment) {
		try {
			HashMap<String, String> header = new HashMap<String, String>();
			header.put("name", varName);
			header.put("type", "string-array");
			header.put("nrLines", "" + strings.length);
			header.put("comment", comment);

			this.writeDataHeader(header);

			for (int i = 0; i < strings.length; i++) {
				this.output.writeString(strings[i]);
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}


	@Override
	public void close() {
		try {
			this.output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void flush() {
		try {
			this.output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
