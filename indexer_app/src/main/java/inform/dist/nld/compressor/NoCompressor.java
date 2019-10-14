package inform.dist.nld.compressor;

import inform.dist.nld.gist.Gist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.lf5.util.StreamUtils;

/**
 * Dummy Compressor implementation that does not compress.
 * @author dadi
 */
public class NoCompressor extends AbstractCompressor {

	@Override
	public byte[] compress(Gist s) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		s.writeTo(baos);
		return baos.toByteArray();
	}

	@Override
	public long getComplexity(Gist s) {
		CountingOutputStream cos = new CountingOutputStream();
		s.writeTo(cos);
		return cos.getCounter();
	}

	@Override
	public String getSpecificExtension() {
		return "";
	}

	@Override
	public byte[] uncompress(InputStream inputStream) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			StreamUtils.copy(inputStream, baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}

}
