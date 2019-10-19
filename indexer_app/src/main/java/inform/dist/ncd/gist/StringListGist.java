package inform.dist.ncd.gist;

import inform.dist.Constants;
import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.compressor.Compressors;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Stores the text as a rows of text lines.
 *
 * upd: Is this useful? I'd say this is deprecated
 * @author dadi
 *
 */
@Deprecated
public abstract class StringListGist extends AbstractGist {
	
	List<String> gist;


	// if this string was retrieved from a compressed format (a .bz2 file for ex), this attribute is the compressor which was used to
	// decompress it.
	Compressor initialCompressor = null;
	URL resource;
	
//	public StringListGist(List<String> list) {
//		this.string = new ArrayList<String>();
//		this.string.addAll(list);
//	}
	
//	public StringListGist(String string) {
//		String[] strings = string.split(Constants.GIST_CONTEXT_SEPARATOR);
//		List<String> list = Arrays.asList(strings);
//		this.string = list;
//	}


	/**
	 * use this constructor when the string is stored already compressed
	 */
	public StringListGist(InputStream inputStream, Compressor compressor) {
		this.initFromInputStream(inputStream, compressor);
	}

	protected void initFromInputStream(InputStream inputStream, Compressor compressor) {
		byte[] bytes = compressor.uncompress(new BufferedInputStream(inputStream));
		String s = new String(bytes, StandardCharsets.UTF_8);
		String[] strings = s.split(Constants.GIST_CONTEXT_SEPARATOR);
		this.gist = Arrays.asList(strings);

	}

	protected void initFromUrl(URL url, Compressor compressor) {
		this.resource = url;
		final String s = url.toExternalForm();
		String extension = s.substring(s.lastIndexOf("."));
		if (extension.startsWith("."))
			extension = extension.substring(1);

		if (compressor == null)
			this.initialCompressor = Compressors.REGISTRY.get(extension);
		else
			this.initialCompressor = compressor;

		try {
			this.initFromInputStream(url.openStream(), this.initialCompressor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public StringListGist(URL url, Compressor compressor) {
		initFromUrl(url, compressor);
	}

	public StringListGist(URL url) {
		this(url, null);
	}

	public StringListGist(File file) {
		try {
			initFromUrl(file.toURI().toURL(), null);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public int nrLines() {
		return this.gist.size();
	}


	
	public void writeToSorted(OutputStream os) {
		int len = gist.size();
		for (int i = 0; i < len; i++) {
			try {
				String s = gist.get(i);
				String[] words = s.split(" ");
				Arrays.sort(words);
				int words_length = words.length;
				for (int j = 0; j < words_length; j++) {
					String w = words[j];
					os.write(w.getBytes(Constants.UTF8_ENCODING));
					if (j != words_length - 1)
						os.write(' ');
				}
				if (i != len - 1)
					os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void writeTo(OutputStream os) {
		int len = gist.size();
		for (int i = 0; i < len; i++) {
			try {
				String s = gist.get(i);
				os.write(s.getBytes(Constants.UTF8_ENCODING));
				
				if (i != len - 1)
					os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
//	@Override
//	public Gist clone() {
//		return new StringListGist(this.string);
//	}
	
	public List<String> getStringList() {
		return gist;
	}

//	@Override
//	public List<Gist> getSubgists(int maxSizeInBytes) {
//		List<Gist> subgists = new ArrayList<Gist>();
//		List<String> crtSubgist = new ArrayList<String>();
//		int crtCounter = 0;
//
//		for (String s : this.string) {
//			if (crtCounter > maxSizeInBytes) {
//				subgists.add(new StringListGist(crtSubgist));
//				crtSubgist = new ArrayList<String>();
//				crtCounter = 0;
//			}
//			crtSubgist.add(s);
//			crtCounter += s.length() + 1;
//		}
//
//		if (crtSubgist.size() > 0)
//			subgists.add(new StringListGist(crtSubgist));
//
//		return subgists;
//	}

	@Override
	public InputStream openStreamForReading() {
		throw new RuntimeException("undefined");
	}

//	@Override
//	public OutputStream openStreamForWriting() {
//		throw new RuntimeException("undefined");
//	}

	@Override
	public long getSizeInBytes() {
		long result = 0;
		for (String s: this.gist) {
			result += (s.length() + 1);
		}
		return result;
	}

	public Compressor getInitialCompressor() {
		return initialCompressor;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
//		int counter = 0;
		for (String s : this.gist) {
			sb.append(s).append("\n");
//			if (counter++ > 5) {
//				sb.append("[...]");
//				break;
//			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		StringListGist slg = (StringListGist) obj;
		return this.gist.equals(slg.gist);
	}
	
}
