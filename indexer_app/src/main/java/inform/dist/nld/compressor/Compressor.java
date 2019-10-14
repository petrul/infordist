package inform.dist.nld.compressor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import inform.dist.nld.gist.Gist;

public interface Compressor {

	enum CmprType { BZIP2, GZIP, PPM, NONE }

	public long getComplexity(Gist s);
	
	public byte[] compress(Gist s);
	
	public byte[] uncompress(InputStream inputStream);
	
	public String getSpecificExtension();
}
