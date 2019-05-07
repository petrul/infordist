import org.apache.commons.lang.time.StopWatchimport inform.dist.nld.compressor.Bzip2Compressorimport org.apache.commons.compress.compressors.bzip2.*import java.io.*import inform.dist.nld.cache.*import inform.dist.Constantsimport inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*
import java.util.concurrent.*;

/* * converts a directory of text gists to a directory of binary short-coded gists */
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
	binGistDir 	= "gists.bin.bz2"	new File(binGistDir).mkdirs();
	
	println "got ${terms.size()} terms, will convert gists in $textGistDir to binaries in $binGistDir...";
	compressor = new Bzip2Compressor()
	def cache = new FsBinaryGistDirectory(textGistDir, compressor);
	//GistRetriever calc = new GistRetriever(index, cache)
	//analyzer = new SnowballAnalyzer('English')
	
	executorService = Executors.newFixedThreadPool(nThreads) 
	Map terms_map = IntCodeGist.termCodes(terms)		def job = { term, destFile ->		StopWatch watch = new StopWatch(); watch.start();		println "starting converting [$term]..."		def stringlistgist = cache.getGist(term)		def codegist = new BinaryGist(stringlistgist, terms_map, nr450kbBlocks * 450 * 1000)				def file_stream = new BufferedOutputStream(new FileOutputStream(destFile))		//file_stream.write(Bzip2Compressor.HEADER)		def bz2stream = new BZip2CompressorOutputStream(file_stream)		codegist.writeTo(bz2stream)		bz2stream.close();				println "done with [$term], stored to $destFile. took ${watch}."	}	
	new File(textGistDir).list().each {
		String encoded_term = it.substring(0, it.indexOf('.'));		term = URLDecoder.decode(encoded_term, Constants.UTF8_ENCODING)		destFile = new File(binGistDir, "${encoded_term}.bz2")		if (destFile.exists()) {			println "$term done, skipping..."		} else {			def ajob = job.curry(term, destFile) as Runnable;			executorService.execute(ajob)			while (executorService.activeCount >= nThreads) { Thread.sleep (2 * 1000) }		}
	}

	executorService.shutdown();
}

this.main(args)
