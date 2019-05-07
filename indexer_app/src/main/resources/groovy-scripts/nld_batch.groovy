import org.apache.commons.lang.time.StopWatch
import java.util.concurrent.TimeUnit
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.lucene.*;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*
import matrix.store.*
import java.util.concurrent.*

//NR_THREADS = 10

/*
 * calculate distances between all words
 */

def main(String[] args) {
	if (args.length < 4)
		throw new IllegalArgumentException("expected arguments: indexLocation nrThreads from_term to_term")
	i = 0;
	indexLocation = args[i++];
	nrThreads = Integer.parseInt(args[i++]);
	from_term = Integer.parseInt(args[i++]);
	to_term = Integer.parseInt(args[i++]);
	
	index = IndexReader.open(indexLocation)
	String[] terms = new IndexUtil (index).getTermsOrderedByFreqDesc(20)[0..<60000].collect {it.term}
	index.close(); index = null;
	println "got ${terms.size()} terms"
	
	compressor = new GzipCompressor()
	cacheDirName = "cache"
	cache = new FsBinaryGistDirectory(new File(cacheDirName));
	
	print "creating or opening matrix $cache... "
	TermMatrixRW matrix = new TermMatrixRW(terms, new File("nld-matrix"), nrThreads);
	println "done."
	
	BinaryGistComplexity gc = new BinaryGistComplexity(new File(cacheDirName))
	
	def job = {String main_term, String[] terms_to_compare ->
		for (term2 in terms_to_compare) {
			StopWatch watch = new StopWatch(); watch.start();
			int cc = (main_term == term2 ? 0 : gc.getCombinedComplexity(main_gist, term2));
			
			matrix.setCombinedComplexity(main_term, term2, cc);
			println ("$main_term - $term2 = $cc, took $watch")
		}
	}
	
	executorService = Executors.newFixedThreadPool(nrThreads)
	
	//terms = terms[200..<terms.size()]
	terms = terms[from_term..<to_term]
	
	println "setting complexities first..."
	terms.each { 
		if (matrix.getComplexity(it) == -1)
			matrix.setComplexity(it, (int)gc.getComplexity(it))
	}
	for (main_term in terms) {
		List row = matrix.getCombinedComplexityRow(main_term)
		
		println "calculating neighbourhood for term $main_term ..."
		String[] notDone = terms.findAll {
			matrix.getTermIndex(it) >= matrix.getTermIndex(main_term) && // this is a triangular matrix
			matrix.getCombinedComplexity(main_term, it) == -1 
		}
		if (notDone.length == 0) continue;
		main_gist = gc.decompressGzip(main_term)
		int c = gc.getComplexity(main_term)
		matrix.setComplexity(main_term, c)
		
		def ajob = job.curry(main_term, notDone) as Runnable;
		executorService.execute(ajob)
		
		while (executorService.activeCount >= nrThreads) { Thread.sleep (2 * 1000) }
	}
	
	executorService.shutdown();
	executorService.awaitTermination(10, TimeUnit.DAYS)
	
}

this.main(args)
