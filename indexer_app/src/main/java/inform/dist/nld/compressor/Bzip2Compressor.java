package inform.dist.nld.compressor;

import inform.dist.nld.gist.Gist;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

public class Bzip2Compressor extends AbstractCompressor{

	@Override
	public long getComplexity(Gist s) {
		try {
			StopWatch watch = new StopWatch(); watch.start();
			CountingOutputStream counterStream = new CountingOutputStream();
			BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(counterStream);
			s.writeTo(bzos);
			bzos.flush();
			bzos.close();
			
			long result = counterStream.getCounter();
			if (LOG.isDebugEnabled()) LOG.debug("done compressing string of nrLines " + s.nrLines() + ", result is " + result  +" bytes long, took " + watch);
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] compress(Gist s) {
		try {
			StopWatch watch = new StopWatch(); watch.start();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(bytes);
			s.writeTo(bzos);
			bzos.close();

			byte[] result = bytes.toByteArray();

			if (LOG.isDebugEnabled())  
				LOG.debug("done compressing string of nrLines " + s.nrLines() + ", result is " + result  +" bytes long, took " + watch);
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
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

}
