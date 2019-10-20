package inform.dist.ncd.compressor;

import inform.dist.ncd.gist.Gist;

import java.io.*;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

public class Bzip2Compressor extends AbstractCompressor{

	static Logger LOG = Logger.getLogger(Bzip2Compressor.class);

	@Override
	public String getSpecificExtension() {
		return ".bz2";
	}

	@Override
	public byte[] uncompress(InputStream inputStream) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(new BufferedInputStream(inputStream));
			int c;
			while ((c = bzip2.read()) != -1) {
				bytes.write(c);
			}
			return bytes.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public OutputStream specificStream(OutputStream out) {
		try {
			return new BZip2CompressorOutputStream(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
