package store.matlabfile;

import org.junit.Assert;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parser of a .matlabfile file; you can get streams of numbers or strings;
 * <p>
 * Main limitation: you can only read one variable from a Reader with an
 * instance of this class. If you want to read the second, you need to create a
 * new instance.
 * 
 * @author dadi
 * 
 */
public class MatTextFileReader implements MatReader {

	private LineNumberReader 	reader;

	public MatTextFileReader(File f) {
		try {
			if (!f.exists())
				throw new IllegalArgumentException("file [" + f + "] does not exist, cannot open it for reading");
			this.reader = new LineNumberReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	
	public MatTextFileReader(Reader r) {
		this.reader = new LineNumberReader(r);
	}

	
	/* (non-Javadoc)
	 * @see inform.dist.serialization.MatReader#getFirstNumberStream()
	 */
	public NumberStream getFirstNumberStream() {
		return this.getNumberStream("*");
	}

	
	/* (non-Javadoc)
	 * @see inform.dist.serialization.MatReader#getNumberStream(java.lang.String)
	 */
	public NumberStream getNumberStream(String name) {
		NumberStream result = null;

		boolean found = false;

		Pattern keyValuePattern = Pattern.compile("^\\s*#\\s*(\\w+)\\s*:\\s*(.+)$");

		String line;
		try {
			Map<String, String> map = new HashMap<String, String>();

			while ((line = this.reader.readLine()) != null) {

				Matcher matcher = keyValuePattern.matcher(line);

				if (matcher.find()) {
					String key = matcher.group(1);
					String value = matcher.group(2);
					if ("name".equalsIgnoreCase(key) && ("*".equals(name) || name.equalsIgnoreCase(value))) {
						found = true;
					}
					map.put(key, value);
					reader.mark(1000 * 1000); // the first data line can have
					// max 1MB
				} else {
					if (found) {
						reader.reset(); // unread the last line which is
						// actually the first data line
						break;
					}
				}
			}

			if (! found)
				throw new IllegalArgumentException("cannot find variable named [" + name + "]");

			
			if ("matrix".equalsIgnoreCase(map.get("type"))) {
				if (map.get("rows") == null)
					throw new IllegalStateException("no rows information about variable [" + name + "]");
				if (map.get("columns") == null)
					throw new IllegalStateException("no columns information about matrix [" + name + "]");

				result = new TextNumberStream(
						map.get("name"), 
						this.reader, 
						Integer.parseInt(map.get("rows")), 
						Integer.parseInt(map.get("columns")));
			} else if ("string-array".equalsIgnoreCase(map.get("type"))) {
				String ssize = map.get("size");
				int size;
				try {
					size = Integer.parseInt(ssize);
				} catch (Exception e) {
					throw new IllegalStateException("cannot parse size for string-array " + name, e);
				}
				result = new TextNumberStream(map.get("name"), this.reader, size, 2);
			} else
				throw new IllegalStateException("variable [" + name + "] of type [" + map.get("type")
						+ "] is not a matrix or string array");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see inform.dist.serialization.MatReader#getStringArray(java.lang.String)
	 */
	public String[] getStringArray(String varName) {
		NumberStream stream = this.getNumberStream(varName);
		String[] result = new String[stream.getRows()];
		for (int i = 0; i < stream.getRows(); i++) {
			String[] crtLine = stream.nextStringRow();
			Assert.assertEquals(2, crtLine.length);
			String crtString = crtLine[1];
			int endIndex = crtString.length() - 1;

			Assert.assertTrue("expecting dblquote at beginning for [" + crtString + "], line " + i, crtString.charAt(0) == '"');
			Assert.assertTrue("expecting dblquote at the end for [" + crtString + "], line " + i, crtString.charAt(endIndex) == '"');

			try {
				result[i] = URLDecoder.decode(crtString.substring(1, endIndex), Constants.UTF8_ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public Long getLongScalar(String varName) {
		NumberStream stream = this.getNumberStream(varName);
		if (stream == null)
			return null;
		if (stream.getRows() != 1 || stream.getColumns() != 1)
			throw new IllegalArgumentException("variable " + varName + "does not point to a 1x1 matrix  as expected");
		return stream.nextLong();
	}

	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static Logger LOG = Logger.getLogger(MatTextFileReader.class.getCanonicalName());
}
