package inform.dist.ncd.gist;

import inform.dist.ncd.compressor.Compressor;

import java.io.File;

/**
 * a directory of textual gists (StringListGist)
 * @author dadi
 */
public class FsStringListGistDirectory extends AbstractFilesystemGistDirectory {


	public FsStringListGistDirectory(File directory, Compressor compressor) {
		super(directory, compressor);
	}

	public FsStringListGistDirectory(File directory) {
		super(directory);
	}

	public FsStringListGistDirectory(String directory, Compressor compressor) {
		super(directory, compressor);
	}

	@Override
	protected Gist newGist(byte[] bytes) {
//		return new StringListGist(new String(bytes, Constants.UTF8_CHARSET));
		throw new RuntimeException("undefined");
	}

}
