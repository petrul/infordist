package inform.dist.ncd.compressor;

import java.io.InputStream;
import java.io.OutputStream;

import inform.dist.ncd.gist.Gist;

public interface Compressor {

	enum CmprType { BZIP2, GZIP, PPM, NONE }

	long getComplexity(Gist s);
	
	void compress(Gist s, OutputStream outputStream);
	
	byte[] uncompress(InputStream inputStream);

	OutputStream specificStream(OutputStream out);
	
	String getSpecificExtension();
}
