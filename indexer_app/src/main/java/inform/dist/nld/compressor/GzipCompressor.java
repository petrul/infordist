package inform.dist.nld.compressor;

import inform.dist.nld.cache.Gist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

public class GzipCompressor implements Compressor {

//	@Override
//	public long getComplexity(String s) {
//		try {
//			StopWatch watch = new StopWatch();
//			watch.start();
//			CountingOutputStream counter = new CountingOutputStream();
//			GZIPOutputStream gzos = new GZIPOutputStream(counter);
//			gzos.write(s.getBytes(Constants.ENCODING));
//			gzos.flush();
//			gzos.close();
//
//			long result = counter.getCounter();
//			if (LOG.isDebugEnabled()) LOG.debug("done compressing string of size " + s.length() + ", result is " + result + " bytes long, took " + watch);
//			return result;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

	public long getComplexity(Gist gist) {
		try {
			StopWatch watch = new StopWatch();
			watch.start();
			CountingOutputStream counter = new CountingOutputStream();
			GZIPOutputStream gzos = new GZIPOutputStream(counter);
			
			gist.writeTo(gzos);
			
			gzos.flush();
			gzos.close();

			long result = counter.getCounter();
			if (LOG.isDebugEnabled()) LOG.debug("done compressing string of size " + gist.size() + ", result is " + result + " bytes long, took " + watch);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public byte[] compress(Gist s) {
		try {
			StopWatch watch = new StopWatch();
			watch.start();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			GZIPOutputStream gzos = new GZIPOutputStream(bytes);
			s.writeTo(gzos);
			//gzos.write(s.getBytes(Constants.ENCODING));
			gzos.flush();
			gzos.close();

			byte[] result = bytes.toByteArray();
			if (LOG.isDebugEnabled()) LOG.debug("done compressing string of size " + s.size() + ", result is " + result.length + " bytes long, took " + watch);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static Logger LOG = Logger.getLogger(GzipCompressor.class);

	@Override
	public String getSpecificExtension() {
		return ".gz";
	}

	@Override
	public byte[] uncompress(InputStream inputStream) {
		throw new RuntimeException("unimplemented");
	}

}
