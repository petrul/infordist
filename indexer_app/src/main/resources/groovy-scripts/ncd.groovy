/*

 	Compute distance matrix between all files in a directory of bzipped objects.

 	Each file must already be bzipped and have the .bz2 extension.

	NLD normalized lucene distance is my terminology for NCD, normalized compression distance from Vitanyi & Cilibrasi 2005
*/

import inform.dist.nld.compressor.Bzip2Compressor
import inform.dist.nld.gist.StringListGist
import matrix.store.IntMatrixStore
import matrix.store.TermMatrix
import matrix.store.TermMatrixRW
import org.apache.commons.cli.*
import org.apache.commons.lang.time.StopWatch
import org.apache.log4j.Logger

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

//NR_THREADS = 10

/*
 * calculate distances between all words
 */

cli_options = [:]

LOG = Logger.getLogger(ncd.class);


def computeNcdRowForTerm(final String main_term, final StringListGist main_gist, final String[] terms_to_compare, final TermMatrix matrix) {

	Bzip2Compressor bz2 = new Bzip2Compressor();
	final String inputDir = cli_options.indir

	for (term2 in terms_to_compare) {
		StopWatch watch = new StopWatch(); watch.start()


		println ("starting cc computation for $main_term - $term2 on [${Thread.currentThread().getName()}]...")

		int cc;
		if (main_term == term2)
			cc = 0
		else {
			final File t2_file = new File(inputDir, term2 + ".bz2")
			final byte[] bytes = bz2.uncompress(new BufferedInputStream(new FileInputStream(t2_file)))
			final String str = new String(bytes, StandardCharsets.UTF_8)
			StringListGist slg = new StringListGist(str)

			StringListGist combined = main_gist.combine(slg)
			cc = bz2.getComplexity(combined)
		}

		matrix.setCombinedComplexity(main_term, term2, cc);

		println ("finished $main_term - $term2 = $cc on [${Thread.currentThread().getName()}], took ${watch}.")
	}
}


def main1(String[] args) {

	this.parseCommandLine();

	nrThreads = cli_options.nrthreads
	outdir = cli_options.outdir
	indir = cli_options.indir

	Bzip2Compressor compressor = new Bzip2Compressor()

	println "opening indir ${indir}"
	if (indir == null || !new File(indir).exists() || ! new File(indir).isDirectory()) {
		printUsage(cli_options)
		throw new IllegalArgumentException("indir should exist and be a directory: $indir")
	}

	String[] bz2_files = new File(indir).list()
		bz2_files.each { assert it.endsWith(".bz2")}
	String[] terms = bz2_files.collect { it.substring(0, it.length() - 4) }
		terms.each { assert ! it.contains(".")}

	println "${terms.length} terms"

	boolean already_existing = new File(outdir).exists()
	TermMatrixRW matrix = new TermMatrixRW(terms, new File(outdir), "NCD distances for files in ${indir}")
	if (!already_existing) {
		println "opened new matrix at ${outdir}"
	} else
		println "opened already existing term matrix ${outdir}"


	def executorService = Executors.newFixedThreadPool(cli_options.nrthreads)
	
	//terms = terms[200..<terms.size()]
//	terms = terms[from_term..<to_term]
	
	println "setting complexities first..."
	terms.each { 
		if (matrix.getComplexity(it) == -1) {
			int len = new File(indir, it + ".bz2").length()
			matrix.setComplexity(it, len)
		}

	}

	for (main_term in terms) {
		List<Integer> row = matrix.getCombinedComplexityRow(main_term)
		
		println "calculating neighbourhood for term $main_term ..."
		String[] notDone = terms.findAll {
			matrix.getTermIndex(it) >= matrix.getTermIndex(main_term) && // this is a triangular matrix
			matrix.getCombinedComplexity(main_term, it) == -1 
		}

		if (notDone.length == 0)
			continue

		String main_gist_str = new String(compressor.uncompress(new BufferedInputStream(new FileInputStream(new File(indir, main_term + ".bz2")))), java.nio.charset.StandardCharsets.UTF_8)
		int c = main_gist_str.length(); // length of the compressed file
		matrix.setComplexity(main_term, c)

		def main_gist = new StringListGist(main_gist_str)
		
		def ajob = { term, term_gist, term_arr, mtrx -> computeNcdRowForTerm(term, term_gist, term_arr, mtrx) }.curry(main_term, main_gist, notDone, matrix) as Runnable;
		executorService.execute(ajob)
		
		while (executorService.activeCount >= nrThreads) { Thread.sleep (2 * 1000) }
	}
	
	executorService.shutdown();
	executorService.awaitTermination(10, TimeUnit.DAYS)
	
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
		System.out.flush()


		cli_options.enforce_recomputation = cmd.getOptionValue("enforce_recompution") ?: false

		cli_options.outdir = cmd.getOptionValue("outdir")  ?: "~/ncd-matrix"
		cli_options.outdir = cli_options.outdir.replace('~', System.getProperty("user.home"))
		cli_options.indir = cmd.getOptionValue("input-dir")  ?: "~/files-dir"
		cli_options.indir = cli_options.indir.replace('~', System.getProperty("user.home"))

		cli_options.nrthreads = cmd.getOptionValue("nrthreads") ?: 8
		cli_options.compressor = cmd.getOptionValue("compressor") ?: "bz2"


		LOG.info(cli_options)

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
//		ExtractTermFrequenciesMatrixFromPositionalIndex ngdCalc = new ExtractTermFrequenciesMatrixFromPositionalIndex(
//				cmd.getOptionValue("index"), minFreq, nrTerms);

//		if (cmd.hasOption("maxdocs")) {
//			int maxdocs = Integer.parseInt(cmd.getOptionValue("maxdocs"));
//			LOG.info("will limit document inspection to " + maxdocs);
//			ngdCalc.setMaxDocumentsToInspect(maxdocs);
//		}

//		if (cmd.hasOption("o")) {
//			String outdir = cmd.getOptionValue("o");
//			ngdCalc.setOutDir(outdir);
//		}

		LOG.info("will output to directory [" + new File(cli_options.outdir).getAbsolutePath() + "]");

//		ngdCalc.run();

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
			.withDescription("recomputes all calculations, disregarding possibly already done computation")
			.withLongOpt("enforce-recompute")
			.hasArg(false)
			.create("f");
	options.addOption(opt);
	opt = OptionBuilder
			.withLongOpt("nrthreads")
			.withDescription("number of concurrent parallel threads")
			.withArgName("number")
			.hasArg()
			.create("n");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("directory")
			.hasArg()
			.isRequired(false)
			.withDescription("directory containing resulting cooccurrences, NCD and UNCD matrices")
			.withLongOpt("outdir")
			.create("o");
	options.addOption(opt);
	opt = OptionBuilder
			.withArgName("bz2, gz")
			.hasArg()
			.isRequired(false)
			.withDescription("compressor name")
			.withLongOpt("compressor")
			.create("c");
	options.addOption(opt);
	opt = OptionBuilder
			.withLongOpt("input-dir")
			.withArgName("directory")
			.hasArg()
			.isRequired(true)
			.withDescription("the directory of files")
			.create('i');
	options.addOption(opt);
//	opt = OptionBuilder
//			.withArgName("number")
//			.hasArg()
//			.isRequired(false)
//			.withDescription("size in terms of the text window : two terms are counted as co-occurring if they are in the same window, default 20")
//			.withLongOpt("windowsize")
//			.create('w');
//	options.addOption(opt);
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
	formatter.printHelp("ncd-batch.groovy indexLocation nocache|cache term1 term2 ...\");", options)
}


this.main1(args)



