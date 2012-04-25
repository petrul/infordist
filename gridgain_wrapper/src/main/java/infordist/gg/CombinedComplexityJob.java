package infordist.gg;

import inform.dist.Constants;
import inform.dist.nld.GistComplexity;
import inform.dist.nld.cache.AbstractFilesystemGistDirectory;
import inform.dist.nld.cache.FsBinaryGistDirectory;
import inform.dist.nld.cache.Gist;
import inform.dist.nld.compressor.Bzip2Compressor;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;


public class CombinedComplexityJob implements Callable<CombinedComplexityJobResult>, Serializable {

	private static final long serialVersionUID = 1L;

	File 		cacheDir;
	String 		mainTerm;
	String[] 	termsToCompare;

	public CombinedComplexityJob(File cacheDir, String mainTerm, String[] termsToCompare) {
		this.cacheDir = cacheDir;
		this.mainTerm = mainTerm;
		this.termsToCompare = termsToCompare;
		
	}

	@Override
	public CombinedComplexityJobResult call() {
		LOG.info("calculating combined complexity of " + this.mainTerm + " to " + this.termsToCompare.length + " comparison terms...");
		StopWatch watch = new StopWatch(); watch.start();
		
		int[] results = new int[termsToCompare.length];
		for (int i = 0; i < termsToCompare.length; i++) {
			results[i] = Constants.MATRIX_CELL_UNINITIALIZED;
		}
		
		try {
			Bzip2Compressor bzip2 = new Bzip2Compressor();
			AbstractFilesystemGistDirectory cache = new FsBinaryGistDirectory(cacheDir, bzip2);
			GistComplexity gc = new GistComplexity(cacheDir, bzip2);

			String term1 = this.mainTerm;
			Gist gist1 = cache.getGist(term1);
			if (gist1 == null) 
				throw new IllegalArgumentException("cannot find binary gist for main term [" + term1 + "]");

			
			for (int i = 0; i < this.termsToCompare.length; i++) {
				String term2 = this.termsToCompare[i];
				Gist gist2 = cache.getGist(term2);
				if (gist2 == null)  {
					LOG.warn("cannot find binary gist for term [" + term2 + "], will skip...");
					continue;
				}
				long cc = gc.calculateGistCombinedComplexity(gist1, gist2);
				
				LOG.info("C(" + term1 + ", " + term2 + ") = " + cc);
				results[i] = (int) cc;
			}
			LOG.info("done [" + this.mainTerm + "], " + this.termsToCompare.length + " terms compared, took " + watch);
			return new CombinedComplexityJobResult(this.mainTerm, this.termsToCompare, results);
		} catch (RuntimeException e) {
			throw e;
		}
		
	}

	static Logger LOG = Logger.getLogger(CombinedComplexityJob.class);
}
