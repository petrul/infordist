package inform.dist.nld;

import inform.dist.Constants;
import inform.dist.nld.gist.AbstractGist;
import inform.dist.nld.gist.Gist;
import inform.dist.nld.gist.combining.GistCombiningPolicy;
import inform.dist.nld.compressor.Compressor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Assert;

/**
 * Given a directory with gzipped gists, use this class to get the size of those gists and 
 * to calculate the size of combined gists.
 * 
 * @author dadi
 * @deprecated  possibly over-architectured code for accessing a directory of zipped files
 *
 */
public class GistComplexity {
	
	File dir;
	
	Compressor compressor;
	
	final int BLOCK_COMBINE_SIZE = 900 * 1000 / 2;
	
	public GistComplexity(File gistDirectory, Compressor compressor) {
		Assert.assertTrue(gistDirectory.exists());
		this.dir = gistDirectory;
		this.compressor = compressor;
	}
	

	@Deprecated
	public long getGistFileSize(String term) {
		return this.getComplexity(term);
	}

	private File getFile(String term) {
		try {
			return new File(this.dir, URLEncoder.encode(term,Constants.UTF8_ENCODING) + compressor.getSpecificExtension());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * this is a shortcut: it simply reads the size of the already compressed gist file.
	 */
	public long getComplexity(String x) {
		File file = this.getFile(x);
		if (!file.exists()) throw new IllegalArgumentException("no gist exists for term " + x);
		return file.length();
	}
	
	public long calculateGistComplexity(Gist gist) {
		return this.compressor.getComplexity(gist);
	}
	
	public long calculateGistCombinedComplexity(Gist gist1, Gist gist2) {
//		Gist both = gist1.clone();
		Gist both = gist1.combine(gist2, GistCombiningPolicy.Policy.INTERLACE_EVEN);
		return this.compressor.getComplexity(both);
	}
	
	/**
	 * you may use this if repeatedly calculating combined complexities of the same x to several other words
	 */
	public long getCombinedComplexity(AbstractGist gist1, Gist gist2) {
		Gist bothGists = (Gist) gist1.combine(gist2);
		
		long cb = this.compressor.getComplexity(bothGists);
		return cb;
	}
}
