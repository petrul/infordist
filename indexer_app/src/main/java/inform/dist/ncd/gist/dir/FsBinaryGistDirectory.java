package inform.dist.ncd.gist.dir;

import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.gist.Gist;

import java.io.File;

@Deprecated
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
		throw new RuntimeException("undefined");
//		return new BinaryGist(bytes);
	}

}
