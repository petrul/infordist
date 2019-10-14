package inform.dist.serialization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.MMapDirectory;
import org.junit.Assert;

import inform.dist.util.BinaryNumberStream;
import inform.dist.util.NumberStream;

public class MatBinaryReader implements MatReader {

	IndexInput input;

	public MatBinaryReader(File file) {
		try {
			this.input = new MMapDirectory().openInput(file.getAbsolutePath());
			this.input.readString(); // header string
			this.input.readString(); // header comment;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Long getLongScalar(String varName) {
		NumberStream numberStream = this.getNumberStream(varName);
		if (numberStream == null)
			return null;
		return numberStream.nextLong();
	}

	
	@Override
	public NumberStream getNumberStream(String name) {
		try {
			String string = this.input.readString();
			Assert.assertEquals(BinFormatConstants.DATA_HEADER, string);
			int mapSize = this.input.readInt();
			HashMap<String, String> map = new HashMap<String, String>(mapSize);
			for (int i = 0; i < mapSize; i++) {
				String key = this.input.readString();
				String value = this.input.readString();
				map.put(key, value);
			}
			if (! map.containsKey("name") || ! name.equals(map.get("name")))
				throw new IllegalStateException("the current object stored is not named [" +
						name + "] : " + map);

			String type = map.get("type");
			if ("matrix".equals(type)) {
				try {
					int i_rows = Integer.parseInt(map.get("rows"));
					int i_cols = Integer.parseInt(map.get("columns"));
					return new BinaryNumberStream(map.get("name"), i_rows, i_cols, this.input);
				} catch (NumberFormatException e) {
					throw new IllegalStateException("could not parse rows or columns attribute for object [" + name + "]", e);
				}
			} else 
			if ("string-array".equals(type)) {
				try {
					int i_rows = 1;
					int i_cols = Integer.parseInt(map.get("nrLines"));
					return new BinaryNumberStream(map.get("name"), i_rows, i_cols, this.input);
				} catch (NumberFormatException e) {
					throw new IllegalStateException("could not parse rows or columns attribute for object [" + name + "]", e);
				}
			} else 
				throw new IllegalStateException("don't know how to read object of type [" + type + "]");
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String[] getStringArray(String varName) {
		NumberStream stream = this.getNumberStream(varName);
		Integer size = stream.getColumns();
		String[] res = new String[size];
		for (int i = 0; i < size; i ++) {
			res[i] = stream.nextString();
		}
		return res;
	}

}
