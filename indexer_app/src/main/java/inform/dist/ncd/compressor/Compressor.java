package inform.dist.ncd.compressor;

import java.io.InputStream;

import inform.dist.ncd.gist.Gist;

public interface Compressor {

	enum CmprType { BZIP2, GZIP, PPM, NONE }

	public long getComplexity(Gist s);
	
	public byte[] compress(Gist s);
	
	public byte[] uncompress(InputStream inputStream);
	
	public String getSpecificExtension();
}
