import inform.dist.nld.cache.FsBinaryGistDirectoryimport org.apache.lucene.analysis.Token
import inform.dist.DistanceCalculator
import inform.dist.nld.*
import matrix.store.TermMatrixRW
import inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*

def main(String[] args) {
	nrterms = 20000
	def indexLocation;
	if (args.length < 4)
		throw new IllegalArgumentException("expected command line : indexLocation nocache|cache term1 term2 ...");

	i_arg = 0;
	indexLocation = args[i_arg ++];
	boolean nocache = (args[i_arg ++] == 'nocache');
	if (nocache) println "will recalcalculate everything, without using cache";
	index = IndexReader.open(indexLocation)
	
	termMatrixFile = new File("termmatrix-${nrterms}.bz2")
	TermMatrixRW tm = TermMatrixRW.checkExists(termMatrixFile, 5)
	if (tm == null) {
		
		String[] terms = new IndexUtil(index).getTermsOrderedByFreqDesc(20)[0..<nrterms].collect {it.term}
		tm = new TermMatrixRW(terms, termMatrixFile, 5)
	}
	
	bzip2 = new Bzip2Compressor()
	
	File gistDir = new File("gists.bz2")
	
	//FilesystemGistCache cache = new FilesystemGistCache(gistDir, bzip2);
	FsBinaryGistDirectory cache = new FsBinaryGistDirectory(gistDir, bzip2)
	GistRetriever gretr = new GistRetriever(index, cache)
	SnowballAnalyzer analyzer = new SnowballAnalyzer('English')
	BinaryGistComplexity gc = new BinaryGistComplexity(gistDir, bzip2)
	
	 
	
	while (true) {
		term1 = analyzer.tokenStream("text", new StringReader(args[i_arg ++])).next(new Token()).term() ;
		term2 = analyzer.tokenStream("text", new StringReader(args[i_arg ++])).next(new Token()).term() ;
		//term2 = args[i_arg ++]

		if (nocache) {
			tm.setComplexity(term1, -1);
			tm.setComplexity(term2, -1);
			tm.setCombinedComplexity(term1, term2, -1);
		}
	
		int c_t1 = tm.getComplexity(term1)
		if (c_t1 == -1) {
			println "retrieving gist and compressing term $term1..."
			gist = gretr.getGist(term1)
			c_t1 = gc.getComplexity(term1)
			tm.setComplexity(term1, c_t1)
		}
		
		int c_t2 = tm.getComplexity(term2)
		if (c_t2 == -1) {
			println "retrieving gist and compressing term $term2..."
			gist = gretr.getGist(term2)
			c_t2 = gc.getComplexity(term2)
			tm.setComplexity(term2, c_t2)
		}
		
		int cc = tm.getCombinedComplexity(term1, term2)
		if (cc == -1) {
			println "combined complexity... "
			cc = gc.getCombinedComplexity(term1, term2)
			tm.setCombinedComplexity(term1, term2, cc)
		}
		
		println "=" * 80
		dist = DistanceCalculator.getNormalizedDistance(c_t1, c_t2, cc)
		
		println "nld($term1, $term2) = $dist ; complexities: $c_t1, $c_t2, $cc"
	}
}

this.main(args)
