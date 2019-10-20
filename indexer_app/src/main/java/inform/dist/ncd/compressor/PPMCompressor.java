package inform.dist.ncd.compressor;

import com.colloquial.arithcode.ArithCodeInputStream;
import com.colloquial.arithcode.ArithCodeOutputStream;
import com.colloquial.arithcode.PPMModel;
import org.apache.log4j.lf5.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PPMCompressor extends AbstractCompressor {

    @Override
    public OutputStream specificStream(OutputStream out) {
        return new ArithCodeOutputStream(out, new PPMModel(8));
    }

    //	@Override
//	public long getComplexity(Gist s) {
//		try {
//			CountingOutputStream cos = new CountingOutputStream();
//			ArithCodeOutputStream ppmos = new ArithCodeOutputStream(cos, new PPMModel(8));
//			s.writeTo(ppmos);
//			ppmos.flush();
//			return cos.getCounter();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
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
