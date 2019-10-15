package inform.dist.ncd;

import inform.dist.Constants;
import inform.dist.ncd.gist.AbstractGist;
import inform.dist.ncd.gist.Gist;
import inform.dist.ncd.compressor.Compressor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Assert;

/**
 * Given a directory with gzipped gists, use this class to get the nrLines of those gists and
 * to calculate the nrLines of combined gists.
 *
 * It's the {@link Gist#computeComplexity(Compressor)}'s business to compute compelexities.
 * 
 * @author dadi
 *
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
	 * this is a shortcut: it simply reads the nrLines of the already compressed string file.
	 */
	public long getComplexity(String x) {
		File file = this.getFile(x);
		if (!file.exists()) throw new IllegalArgumentException("no string exists for term " + x);
		return file.length();
	}
	
	public long calculateGistComplexity(Gist gist) {
		return this.compressor.getComplexity(gist);
	}
	
	public long calculateGistCombinedComplexity(Gist gist1, Gist gist2) {
		throw new RuntimeException("undefined");
//		Gist both = gist1.clone();
//		Gist both = gist1.combine(gist2, GistCombiningPolicy.Policy.INTERLACE_EVEN);
//		return this.compressor.getComplexity(both);
	}
	
	/**
	 * you may use this if repeatedly calculating combined complexities of the same x to several other words
	 */
	public long getCombinedComplexity(AbstractGist gist1, Gist gist2) {
		throw new RuntimeException("undefined");
//		Gist bothGists = (Gist) gist1.combine(gist2);
//
//		long cb = this.compressor.getComplexity(bothGists);
//		return cb;
	}
}
