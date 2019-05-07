import inform.dist.ngd.WeightedTerm
import inform.dist.nld.cache.StringListGist
import org.apache.lucene.analysis.Token
import inform.dist.DistanceCalculator
import inform.dist.nld.*
import matrix.store.TermMatrixRW
import inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.*;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*


def main(String[] args) {
	def nrterms = 20000
	def indexLocation;
	def i_arg = 0;
	def bzip2 = new Bzip2Compressor()
	//def ppm = new PPMCompressor()
	def ppm = bzip2;
	
	//File gistDir = new File("textgists"); FsStringListGistDirectory cache = new FsStringListGistDirectory(gistDir, bzip2);

	File gistDir = new File("binarygists"); def cache = new FsBinaryGistDirectory(gistDir, bzip2);

	println "using compressor $ppm, and gist directory: $cache"
	GistComplexity gc = new GistComplexity(gistDir, ppm)

	def defaultSubgistSize = 400 * 1000
	
	def term1 = args[i_arg++];
	Gist gist1 = cache.getGist(term1); assert gist1 != null ;
	def subgists1 = gist1.getSubgists(defaultSubgistSize).findAll {it.sizeInBytes >= defaultSubgistSize};
	assert subgists1.size > 0
	println "term [$term1] has size ${gist1.sizeInBytes}, in ${subgists1.size()} subgists of size $defaultSubgistSize" 
	
	List orderedNeighbours = []
	List ignoredNeighbours = []
	Map  term_data = [:] // accumulate term statistics so that we can print them later
	def c1_cache = [:]
	args = gistDir.list().collect { it[0..-5] } ;
	while (i_arg < args.size()) {
		def term2 = args[i_arg ++]; 
		println "*" * 30; println term2; 
		def gist2 = cache.getGist(term2); assert gist2 != null;
	
		if (gist2.sizeInBytes < defaultSubgistSize) {
			println "!!!gist [$term2] is too small (${gist2.sizeInBytes}), ignoring..."
			ignoredNeighbours << new WeightedTerm(term2, 100.0);
			continue;
		}


		def subgists2 = gist2.getSubgists(defaultSubgistSize).findAll{it.sizeInBytes >= defaultSubgistSize}
		assert subgists2.size > 0
		
		println "$term1 : ${subgists1.size()} subgists; $term2 : ${subgists2.size()} subgists;"

		def distances = []
		//def nrCompressions = Math.min(5, Math.max(subgists1.size(), subgists2.size()))
		def nrCompressions = 10
		for (i in 0..<nrCompressions) {
			def c2_cache = [:]
			def rnd = new Random()
			int i1 = rnd.nextInt(subgists1.size())
			int i2 = rnd.nextInt(subgists2.size())
			def sg1 = subgists1[i1]
			def sg2 = subgists2[i2]
			int c1 = c1_cache[i1] ?: { c = gc.calculateGistComplexity(sg1) ; c1_cache[i1] = c; c }.call();
			int c2 = c2_cache[i2] ?: { c = gc.calculateGistComplexity(sg2) ; c2_cache[i2] = c; c }.call();
			
			int cc = gc.calculateGistCombinedComplexity(sg1, sg2);
			def dist = DistanceCalculator.getNormalizedDistance(c1, c2, cc);
			distances << dist
			print "$i "
		}

		//println("$term1 - $term2")

		mean = 0
		distances.each {mean += it}
		mean = mean / distances.size()
		std_dev = 0
		distances.each {diff = it - mean; std_dev += (diff * diff); }
		std_dev = Math.sqrt(std_dev / distances.size())

		println "nld($term1, $term2) = $mean ; std_dev = $std_dev"
		
		wt = new WeightedTerm(term2, mean)
		if (std_dev < 0.05) {
			orderedNeighbours << wt
			orderedNeighbours.sort()
			println "neighbours now : $orderedNeighbours"
		} else {
			ignoredNeighbours << wt
			println "std deviation to big, will not take [$term2] into account, ignored neighbours now : $ignoredNeighbours"

		}
		Map tdata = [:]
		//tdata['term'] = wt.term
		tdata['dist'] = wt.weight
		tdata['stddev'] = std_dev
		term_data[wt.term] = tdata
	}
	
	orderedNeighbours.sort()
	println "term [$term1] has size ${gist1.sizeInBytes}, in ${subgists1.size()} subgists of size $defaultSubgistSize" 
	println "[$term1] neighbourhood :"
	println "======================"
	orderedNeighbours.each { println "${it.term}, ${term_data[it.term]['dist']}, ${term_data[it.term]['stddev']} " }
	println "======================"
	println "ignored neighbours: $ignoredNeighbours"
}


/* pass the argument through the analyzer, for stemming */
def analyzeTerm(Analyzer analyzer, String s) {
	analyzer.tokenStream("text", new StringReader(s)).next(new Token()).term()
}


this.main(args)
