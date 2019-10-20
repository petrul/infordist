package inform.dist.ncd.compressor;

import inform.dist.ncd.gist.Gist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

public class GzipCompressor extends AbstractCompressor {


//	public long getComplexity(Gist gist) {
//		try {
//			StopWatch watch = new StopWatch();
//			watch.start();
//			CountingOutputStream counter = new CountingOutputStream();
//			GZIPOutputStream gzos = new GZIPOutputStream(counter);
//
//			gist.writeTo(gzos);
//
//			gzos.flush();
//			gzos.close();
//
//			long result = counter.getCounter();
//			if (LOG.isDebugEnabled()) LOG.debug("done compressing string of nrLines " + gist.nrLines() + ", result is " + result + " bytes long, took " + watch);
//			return result;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
//	@Override
//	public void compress(Gist s, OutputStream outputStream) {
//		try {
//			StopWatch watch = new StopWatch();
//			watch.start();
//
//			GZIPOutputStream gzos = new GZIPOutputStream(outputStream);
//			s.writeTo(gzos);
//			//gzos.write(s.getBytes(Constants.ENCODING));
//			gzos.flush();
//
//			if (LOG.isDebugEnabled()) LOG.debug("done compressing gist, took " + watch);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

	static Logger LOG = Logger.getLogger(GzipCompressor.class);

	@Override
	public String getSpecificExtension() {
		return ".gz";
	}

	@Override
	public byte[] uncompress(InputStream inputStream) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public OutputStream specificStream(OutputStream out) {
		try {
			return new GZIPOutputStream(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
