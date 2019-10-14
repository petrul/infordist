package inform.dist.nld.gist;

import inform.dist.Constants;
import inform.dist.nld.compressor.Compressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Filesystem-based string directory. this is just a directory that compresses its contents.
 * @author dadi
 *
 */
public abstract class AbstractFilesystemGistDirectory implements GistDirectory {

	private File dir;
	Compressor compressor;
	
	abstract protected Gist newGist(byte[] bytes);
	
	public AbstractFilesystemGistDirectory(String directory, Compressor compressor) {
		this(new File(directory), compressor);
	}
	
	public AbstractFilesystemGistDirectory(File directory, Compressor compressor) {
		this(directory);
		this.compressor = compressor;
	}
	
	protected AbstractFilesystemGistDirectory(File directory) {
		if (directory.exists() && ! directory.isDirectory())
			throw new IllegalArgumentException("expected directory : "+ directory);
		if (!directory.exists())
			directory.mkdirs();
		this.dir = directory;
//		this.compressor = new GzipCompressor();
	}
	
	@Override
	public Gist getGist(String term) {
		try {
			File termFile = this.fileName(term); 
			if (!termFile.exists())
				throw new IllegalArgumentException("no string for term [" + term + "] in directory [" + this.dir + "]");
			byte[] decompress = this.compressor.uncompress(new FileInputStream(termFile));
			return this.newGist(decompress);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File fileName(String term) {
		try {
			String fsterm = URLEncoder.encode(term, Constants.UTF8_ENCODING);
			String withExtension = fsterm;
			if (this.compressor.getSpecificExtension() != null) {
				withExtension = withExtension + this.compressor.getSpecificExtension();
			}
			return new File(this.dir, withExtension);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void storeGist(String term, Gist gist) {
		
		try {
			File file = this.fileName(term);
			FileUtils.writeByteArrayToFile(file, this.compressor.compress(gist));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasGist(String term) {
		return this.fileName(term).exists();
	}

	static Logger LOG = Logger.getLogger(AbstractFilesystemGistDirectory.class);
}
