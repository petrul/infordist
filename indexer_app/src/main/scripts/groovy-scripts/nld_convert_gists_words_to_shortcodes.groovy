import org.apache.commons.lang.time.StopWatch
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*
import java.util.concurrent.*;


nThreads = 2
nr450kbBlocks = 10

def main(String[] args) {
	indexLocation = args[0];
	nThreads = Integer.parseInt(args[1]);
	
	def index = IndexReader.open(indexLocation)
	String[] terms = new IndexUtil(index).getTermsOrderedByFreqDesc(200)[0..60000]*.term
	//String[] terms = new IndexUtil(index).getTermsOrderedByFreqDesc(2)*.term
	index.close(); index = null;
	
	textGistDir = "gists.bz2"
	binGistDir 	= "gists.bin.bz2"
	
	println "got ${terms.size()} terms, will convert gists in $textGistDir to binaries in $binGistDir...";
	compressor = new Bzip2Compressor()
	def cache = new FsBinaryGistDirectory(textGistDir, compressor);
	//GistRetriever calc = new GistRetriever(index, cache)
	//analyzer = new SnowballAnalyzer('English')
	
	executorService = Executors.newFixedThreadPool(nThreads) 
	Map terms_map = IntCodeGist.termCodes(terms)
	new File(textGistDir).list().each {
		String encoded_term = it.substring(0, it.indexOf('.'));
	}

	executorService.shutdown();
}

this.main(args)