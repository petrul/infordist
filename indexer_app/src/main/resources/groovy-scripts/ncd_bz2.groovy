/**
 * this computes the NCD using bzip2 between two terms: it receives the terms in command line, it passes them through the analyzer, finds
 * the corresponding file in the gist files dir (or in the index, using GistRetriever) -- then computes the NCD.
 */
import inform.dist.cli.ExtractTermFrequenciesMatrixFromPositionalIndex
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.lucene.analysis.Token
import inform.dist.DistanceCalculator
import inform.dist.nld.*
import matrix.store.TermMatrixRW
import inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import inform.dist.nld.*;
import inform.dist.nld.cache.*;
import inform.dist.nld.compressor.*

def main1(String[] args) {

	this.parseCommandLine()

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
//	BinaryGistComplexity gc = new BinaryGistComplexity(gistDir, bzip2)
	
	 
	
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


def parseCommandLine() {
	Options options = buildOptions();

	try {

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("help")) {
			printUsage(options);
			return;
		}

		System.out.println("****************************************************************************");
		System.out.println("* Semantic tool for computing NCD from a directory of files 			   *");
		System.out.println("****************************************************************************");
		System.out.flush();

		int nrTerms = Integer.parseInt(cmd.getOptionValue("nrterms"));
		LOG.info("" + nrTerms + " terms taken into account in this calculation");
		int minFreq = 20;
		if (cmd.hasOption("minfreq"))
			minFreq = Integer.parseInt(cmd.getOptionValue("minfreq"));
		LOG.info("minimum frequency: " + minFreq);
		String outfile = cmd.getOptionValue("outfile");
		if (outfile == null)
			outfile = "ngd-distances";

//			OutputFormat outputFormat = OutputFormat.BINARY_NIO;
//			if (cmd.hasOption("f")) {
//				String format = cmd.getOptionValue("f");
//				if ("binary".equalsIgnoreCase(format))
//					outputFormat = OutputFormat.BINARY;
//				else
//					if ("text".equalsIgnoreCase(format))
//						outputFormat = OutputFormat.TEXT;
//					else
//						if ("nio".equalsIgnoreCase(format) || format.equalsIgnoreCase("binary-nio"))
//							outputFormat = OutputFormat.BINARY_NIO;
//							else
//								throw new ParseException("bad format [" + format + "], expected binary, binary-nio or text");
//			}
//			LOG.info("will use " + outputFormat + " output format");
		ExtractTermFrequenciesMatrixFromPositionalIndex ngdCalc = new ExtractTermFrequenciesMatrixFromPositionalIndex(
				cmd.getOptionValue("index"), minFreq, nrTerms);

		if (cmd.hasOption("maxdocs")) {
			int maxdocs = Integer.parseInt(cmd.getOptionValue("maxdocs"));
			LOG.info("will limit document inspection to " + maxdocs);
			ngdCalc.setMaxDocumentsToInspect(maxdocs);
		}

		if (cmd.hasOption("o")) {
			String outdir = cmd.getOptionValue("o");
			ngdCalc.setOutDir(outdir);
		}

		LOG.info("will output to directory [" + new File(ngdCalc.getOutDir()).getAbsolutePath() + "]");

		ngdCalc.run();

	} catch (ParseException e) {
		System.err.println("Arguments problem. " + e.getMessage());
		printUsage(options);
	}

}


private static Options buildOptions() {
	Options options = new Options();

	Option opt = OptionBuilder
			.withDescription("print these help instructions")
			.withLongOpt("help")
			.hasArg(false)
			.create("h");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("directory")
			.hasArg()
			.isRequired()
			.withDescription("lucene index")
			.withLongOpt("index")
			.create("i");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("number")
			.hasArg()
			.isRequired()
			.withDescription("number of first terms to get statistics for, in decreasing frequency order")
			.withLongOpt("nrterms")
			.create("n");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("directory")
			.hasArg()
			.isRequired(false)
			.withDescription("directory containing resulting cooccurrences, NGD and UNGD matrices")
			.withLongOpt("outdir")
			.create("o");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("number")
			.hasArg()
			.isRequired(false)
			.withDescription("minimum frequency a term must have in order to be taken into account, default 20")
			.withLongOpt("minfreq")
			.create("f");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("number")
			.hasArg()
			.isRequired(false)
			.withDescription("maximum number of documents to inspect")
			.withLongOpt("maxdocs")
			.create('d');
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("number")
			.hasArg()
			.isRequired(false)
			.withDescription("size in terms of the text window : two terms are counted as co-occurring if they are in the same window, default 20")
			.withLongOpt("windowsize")
			.create('w');
	options.addOption(opt);
//		opt = OptionBuilder
//				.withArgName("text|binary|binary-nio")
//				.hasArg()
//				.isRequired(false)
//				.withDescription("format of the serialized matrix files, can be text, binary or binary-nio, defaults to binary-nio (faster)")
//				.withLongOpt("outputformat")
//				.create();
//		options.addOption(opt);
	return options;
}

private static void printUsage(Options options) {
	HelpFormatter formatter = new HelpFormatter()
	formatter.printHelp("ncd-bz2.groovy indexLocation nocache|cache term1 term2 ...\");", options)
}


this.main1(args)
