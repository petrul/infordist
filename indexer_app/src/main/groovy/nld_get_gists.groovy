import inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*
import java.util.concurrent.*;

/*
 * from a positional index, retrieve gists for terms and store them as compressed files
 */

minfreq = 200
nThreads = 2
def main(String[] args) {
	indexLocation = args[0];
	nThreads = Integer.parseInt(args[1]);
	
	index = IndexReader.open(indexLocation)
	terms = new IndexUtil(index).getTermsOrderedByFreqDesc(minfreq)[0..60000]
	gistDir = "gists.bz2"
	println "got ${terms.size()} terms, will store them in $gistDir...";
	compressor = new Bzip2Compressor()
	//compressor = new GzipCompressor()
	def cache = new FsBinaryGistDirectory(gistDir, compressor);
	GistRetriever calc = new GistRetriever(index, cache)
	//analyzer = new SnowballAnalyzer('English')
	
	executorService = Executors.newFixedThreadPool(nThreads) 

	def job = { termandfreq ->
	    println "calculating gist for ${termandfreq} ..."
	    if (!cache.hasGist(termandfreq.term)) {
	      dist = calc.getGist(termandfreq.term)
	    }
	}

	terms.each {
		def ajob = job.curry(it) as Runnable;
		executorService.execute(ajob)

		while (executorService.activeCount >= nThreads) { Thread.sleep (2 * 1000) }
	}
	executorService.shutdown();
}

this.main(args)