package inform.dist.nld.gist;

import inform.dist.nld.compressor.Compressor;

import java.io.File;

public class FsBinaryGistDirectory extends AbstractFilesystemGistDirectory {


	public FsBinaryGistDirectory(File directory, Compressor compressor) {
		super(directory, compressor);
	}

	public FsBinaryGistDirectory(File directory) {
		super(directory);
	}

	public FsBinaryGistDirectory(String directory, Compressor compressor) {
		super(directory, compressor);
	}

	@Override
	protected Gist newGist(byte[] bytes) {
		return new BinaryGist(bytes);
	}

}
