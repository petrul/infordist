package inform.dist.nld.compressor;

import java.io.InputStream;

import inform.dist.nld.cache.Gist;

public interface Compressor {
	
	public long getComplexity(Gist s);
	
	public byte[] compress(Gist s);
	
	public byte[] uncompress(InputStream inputStream);
	
	public String getSpecificExtension();
}
