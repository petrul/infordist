package ncd.cli
/**
 * Compute distance matrix between all bzipped files in a directory using bzip2.
 *
 * Each file must already be bzipped and have the .bz2 extension.
 *
 * 	NCD is normalized compression distance from Vitanyi & Cilibrasi 2005
 *
 */


import inform.dist.ncd.compressor.Bzip2Compressor
import inform.dist.ncd.gist.FileGist
import inform.dist.ncd.gist.Gist
import inform.dist.ncd.gist.StringGist
import matrix.store.TermMatrix
import matrix.store.TermMatrixRW
import org.apache.commons.cli.*
import org.apache.commons.lang.time.StopWatch
import org.apache.log4j.Logger

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

NR_THREADS = 8

/*
 * calculate distances between all words
 */

cli_options = [:]

LOG = Logger.getLogger(this.class);


def computeNcdRowForTerm(final String main_term, final Gist main_gist, final String[] terms_to_compare, final TermMatrix matrix) {

    Bzip2Compressor bz2 = new Bzip2Compressor();
    final String inputDir = cli_options.indir

    for (term2 in terms_to_compare) {
        StopWatch watch = new StopWatch(); watch.start()

        println("starting [$main_term - $term2] on [${Thread.currentThread().getName()}]...")

        int cc;
        if (main_term == term2)
            cc = 0
        else {
            final File t2_file = new File(inputDir, term2 + ".bz2")
            FileGist g = new FileGist(t2_file)
            cc = main_gist.computeCombinedComplexity(g)
        }

        matrix.setCombinedComplexity(main_term, term2, cc);

        println("finished [$main_term - $term2] = $cc on [${Thread.currentThread().getName()}], took ${watch}.")
    }
}


def main1(String[] args) {

    try {
        parseCommandLine();
    } catch (ParseException e) {
        println e.message
        printUsage(options)
        return
    }

    nrThreads = cli_options.nrthreads
    outdir = cli_options.outdir
    String indir = cli_options.indir
    if (new File(indir).exists() && !new File(indir).isDirectory()) {
        println("input-dir should exist and be a directory: $indir")
        return
    }

    Bzip2Compressor compressor = new Bzip2Compressor()
    println "opening indir ${indir}"

    String[] bz2_files = new File(indir).list()
    bz2_files.each { assert it.endsWith(".bz2") }
    String[] terms = bz2_files.collect { it.substring(0, it.length() - 4) }
    terms.each { assert !it.contains(".") }

    println "${terms.length} terms"

    boolean already_existing = new File(outdir).exists()
    TermMatrixRW matrix = new TermMatrixRW(terms, new File(outdir), "NCD distances for files in ${indir}")
    if (!already_existing) {
        println "opened new matrix at ${outdir}"
    } else
        println "opened already existing term matrix ${outdir}"


    def executorService = Executors.newFixedThreadPool(cli_options.nrthreads)

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
        String[] terms_not_yet_done = terms.findAll {
            matrix.getCombinedComplexity(main_term, it) == -1
        }

        if (terms_not_yet_done.length == 0)
            continue

        File file = new File(indir, main_term + ".bz2")
        Gist main_gist = new StringGist(new FileGist(file))

        def ajob = { term, term_gist, term_arr, mtrx ->
            computeNcdRowForTerm(term, term_gist, term_arr, mtrx)
        }.curry(main_term, main_gist, terms_not_yet_done, matrix) as Runnable

        executorService.execute(ajob)

        while (executorService.activeCount >= nrThreads) {
            Thread.sleep(2 * 1000)
        }
    }

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.DAYS)

}

Options options
def parseCommandLine() {
     options = buildOptions();

//    try {

        System.out.println("****************************************************************************");
        System.out.println("* Semantic tool for computing NCD from a directory of files 			   *");
        System.out.println("****************************************************************************");
        System.out.flush()

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            printUsage(options);
            throw new RuntimeException("help displayed.")
        }

        cli_options.enforce_recomputation = cmd.getOptionValue("enforce_recompution") ?: false

        cli_options.outdir = cmd.getOptionValue("outdir") ?: "ncd-matrix"
        cli_options.outdir = cli_options.outdir.replace('~', System.getProperty("user.home"))
        cli_options.indir  = cmd.getOptionValue("input-dir")
        cli_options.indir  = cli_options.indir.replace('~', System.getProperty("user.home"))

        cli_options.nrthreads  = Integer.parseInt(cmd.getOptionValue("nrthreads")) ?: NR_THREADS
        cli_options.compressor = cmd.getOptionValue("compressor") ?: "bz2"


        println(cli_options)


        println("will output to directory [" + new File(cli_options.outdir).getAbsolutePath() + "]");

}


private static Options buildOptions() {
    Options options = new Options();

    Option opt = Option.builder("h")
            .desc("print these help instructions")
            .longOpt("help")
            .hasArg(false)
            .build()
    options.addOption(opt);
    opt = Option.builder("f")
            .desc("recomputes all calculations, disregarding possibly already done computation")
            .longOpt("enforce-recompute")
            .hasArg(false)
            .build()

    options.addOption(opt);
    opt = Option.builder("n")
            .longOpt("nrthreads")
            .desc("number of concurrent parallel threads")
            .argName("number")
            .hasArg()
            .build()
    options.addOption(opt);
    opt = Option.builder("o")
            .longOpt("outdir")
            .desc("directory containing resulting cooccurrences, NCD and UNCD matrices")
            .argName("directory")
            .hasArg()
            .required(false)
            .build()
    options.addOption(opt);
    opt = Option.builder("c")
            .longOpt("compressor")
            .desc("compressor name")
            .argName("bz2, gz")
            .hasArg()
            .required(false)
            .build()
    options.addOption(opt);
    opt = Option.builder("i")
            .longOpt("input-dir")
            .desc("the directory of files (gists)")
            .argName("directory")
            .hasArg()
            .required(true)
            .build()
    options.addOption(opt);

    return options;
}

private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter()
    formatter.printHelp("ncd.groovy indexLocation nocache|cache term1 term2 ...\");", options)
}

main1(args)



