package inform.dist.ncd.gist;

import inform.dist.Constants;
import inform.dist.ncd.gist.combining.GistCombiningPolicy;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This represents an in-memory Gist. (mainly for tests)
 */
public class StringGist extends AbstractGist {

	protected String string;
	
	public StringGist(String s) {
		this.string = s;
	}

	public StringGist(List<String> lines) {
		this.string = String.join("\n", lines);
	}

	public StringGist(FileGist fileGist) {
		try {
			this.string = IOUtils.toString(fileGist.openStreamForReading());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int nrLines() {
		return string.split("\n").length;
	}

	@Override
	public void writeTo(OutputStream os) {
		try {
			os.write(string.getBytes(Constants.UTF8_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}

	@Override
	public void combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy, OutputStream outputStream) {
		GistCombiningPolicy.REGISTRY.get(combiningPolicy).combine(this,anotherGist, outputStream);
	}

	@Override
	public InputStream openStreamForReading() {
		return new ByteArrayInputStream(this.string.getBytes(StandardCharsets.UTF_8));
	}

//	@Override
//	public OutputStream openStreamForWriting() {
//		throw new RuntimeException("undefined");
//	}

	@Override
	public Gist clone() {
		throw new RuntimeException();
		//return new StringGist(this.string) ;
	}

	public List<String> getStringList() {
		return Arrays.asList(this.string.split("\n"));
	}

	@Override
	public long getSizeInBytes() {
		return this.string.length();
	}

	@Override
	public Iterator<String> iterator() {
		return new StringGistIterator(this);
	}


	class StringGistIterator implements java.util.Iterator<String> {

		StringGist sg;
		String[] lines;
		int crtLine = 0;

		public StringGistIterator(StringGist stringGist) {
			this.sg = stringGist;
			this.lines = sg.string.split("\n");
		}

		@Override
		public boolean hasNext() {
			return crtLine < lines.length;
		}

		@Override
		public String next() {
			return lines[crtLine++];
		}
	}


	@Override
	public String toString() {

		List<String> rowList = Arrays.asList(this.string.split("\n"));
		String ending = "";

		if (rowList.size() > 5) {
			rowList = rowList.subList(0, 4);
			ending = "...";
		}

		return String.join("\n", rowList) + ending;

//		StringBuilder sb = new StringBuilder();
////		int counter = 0;
//		for (String s : this.gist) {
//			sb.append(s).append("\n");
////			if (counter++ > 5) {
////				sb.append("[...]");
////				break;
////			}
//		}
//		return sb.toString();
	}
}
