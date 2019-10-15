package inform.dist.ncd.compressor;

import inform.dist.ncd.gist.Gist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.lf5.util.StreamUtils;

import com.colloquial.arithcode.ArithCodeInputStream;
import com.colloquial.arithcode.ArithCodeOutputStream;
import com.colloquial.arithcode.PPMModel;

public class PPMCompressor extends AbstractCompressor {

	@Override
	public byte[] compress(Gist s) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ArithCodeOutputStream ppmos = new ArithCodeOutputStream(baos, new PPMModel(8));
			s.writeTo(ppmos);
			ppmos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getComplexity(Gist s) {
		try {
			CountingOutputStream cos = new CountingOutputStream();
			ArithCodeOutputStream ppmos = new ArithCodeOutputStream(cos, new PPMModel(8));
			s.writeTo(ppmos);
			ppmos.flush();
			return cos.getCounter();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getSpecificExtension() {
		return ".ppm";
	}

	@Override
	public byte[] uncompress(InputStream inputStream) {
		try {
			ArithCodeInputStream acis = new ArithCodeInputStream(inputStream, new PPMModel(8));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamUtils.copy(acis, baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
