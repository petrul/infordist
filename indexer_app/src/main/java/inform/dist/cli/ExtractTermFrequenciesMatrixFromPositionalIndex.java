package inform.dist.cli;

import inform.dist.Constants;
import inform.dist.util.UpperSymmetricalZeroDiagMatrix;
import inform.dist.util.Util;
import inform.lucene.IndexUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.store.TermMatrixRW;

import org.apache.commons.cli.*;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermPositionVector;

import wiki.indexer.TermAndFreq;
import wiki.indexer.storage.SimpleTermCooccurrenceStorage;

/**
 * from a positional index retrieves
 */
public class ExtractTermFrequenciesMatrixFromPositionalIndex implements Runnable {

	/* only take into account terms with frequence bigger than this */
	private int 	minFreq;
	
	private String 	indexLocation;
	
	/* the number of terms that will be calculated */
	private int 	nrTerms;
	private int 	maxDocumentsToInspect = Integer.MAX_VALUE;
	
	/* the number of terms in a window */
	private int 	windowSize = 20;

	private String 	outDir = "ngdstats";

//	public static enum OutputFormat { BINARY, BINARY_NIO, TEXT } ;
//	OutputFormat 	outputFormat;

	
	public ExtractTermFrequenciesMatrixFromPositionalIndex (String indexLocation, int minFreq, int nrTerms) {
		this.indexLocation = indexLocation;
		this.minFreq = minFreq;
		this.nrTerms = nrTerms;
//		this.outputFormat = format;
	}

	
	public static void main(String[] args) {
		
		Options options = buildOptions();

		try {
			
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("help")) {
				printUsage(options);
				return;
			}

			System.out.println("****************************************************************************");
			System.out.println("* Semantic tool for extraction of term frequencies from a positional index *");
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

	
	@Override
	public void run()  {
		try {
			final IndexReader idxReader = IndexReader.open(new File(this.indexLocation));
			List<TermAndFreq> termList = new IndexUtil(idxReader).getTermsOrderedByFreqDesc(this.minFreq);

			int actualNrTerms = Math.min(termList.size(), this.nrTerms);
			if (actualNrTerms < this.nrTerms) {
				LOG.warn("only could retrieve " + actualNrTerms + " terms with freq > " + this.minFreq + " which less than you wanted (" + this.nrTerms + ")");
				this.nrTerms = actualNrTerms;
			}
			/* a map so we can get a terms's index easily */
			Map<String, Integer> term_id_map = Util.get_term_id_map(termList, actualNrTerms);
			
			SimpleTermCooccurrenceStorage storage = new SimpleTermCooccurrenceStorage(term_id_map);
			
			int counterNonEmptyWindows = 0;
			int documentsToInspect = Math.min(idxReader.maxDoc(), this.maxDocumentsToInspect);
			
			System.err.print("document progress: ");
			for (int d = 0; d < documentsToInspect; d++) {
				if (d % 10000 == 0)
					System.err.print("" + d + "... ");
				
				TermPositionVector termPosVector = (TermPositionVector) idxReader.getTermFreqVector(d, Constants.FIELD_TEXT);
				
				if (termPosVector == null) {
					if (LOG.isDebugEnabled()) {
						Document doc = idxReader.document(d);
						LOG.debug("no term vector for document " + d + ": " + doc.getField("id").stringValue());
					}
					continue;
				}
					
				
				/* we will cut the crt document into smaller documents, called text windows of nrLines this.windowSize */
				Map<Integer, Set<String>> textWindows = new HashMap<Integer, Set<String>>();
				
				String[] terms = termPosVector.getTerms();
				for (int t = 0; t < terms.length; t++) {
					String crtTerm = terms[t];
					int[] positions = termPosVector.getTermPositions(t);
					if (positions == null)
						throw new IllegalArgumentException("index " + this.indexLocation + " has not stored term vectors");
					for (int pos : positions) {
						int windowId = pos / this.windowSize;
						if (! textWindows.containsKey(windowId))
							textWindows.put(windowId, new HashSet<String>());
						textWindows.get(windowId).add(crtTerm);
					}
				}
				
				for (int wid : textWindows.keySet()) {
					List<String> list = new ArrayList<String>(textWindows.get(wid));
					int window_size = list.size();
					
					if (window_size > 0) {
						
						counterNonEmptyWindows++;
						
						for  (int i = 0; i < window_size; i++) {
							String term1 = list.get(i);
							storage.increaseTermFreq(term1);
							for (int j = i + 1; j < window_size; j++) {
								String term2 = list.get(j);
								storage.markCooccurrence(term1, term2);
							}
						}
					}
				}
			}
			
			LOG.info("a total of " + counterNonEmptyWindows + "non-empty text windows");
			this.storeResults(storage, counterNonEmptyWindows, Util.get_term_arr(termList, actualNrTerms));
			
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new RuntimeException(e);
		}
	}
	
	
	private void storeResults(final SimpleTermCooccurrenceStorage storage, long nPages, String[] terms) throws IOException 	{
		
		StopWatch watch = new StopWatch();
		
		String originComment = "created from index [" + new File(this.indexLocation).getAbsolutePath() + "],";  
		
		{
			//basename = ;
			HashMap<String,Long> variables = new HashMap<>();
			variables.put("total_docs", nPages);

			TermMatrixRW exporter = this.newExporter(terms, variables, originComment);
			watch.reset(); watch.start();
			final UpperSymmetricalZeroDiagMatrix cooccurrenceMatrix = storage.getCooccurrenceMatrix();

			

			//exporter. writeStringArray("terms", terms, "terms");
			
			{
				int[] absFreqs = storage.getTermAbsoluteFreq();
				for (int i = 0; i < terms.length; i++) {
					exporter.setComplexity(terms[i], absFreqs[i]);
				}
			}
			
				//int[] absFreqs = storage.getTermAbsoluteFreq();
				for (int i = 0; i < terms.length; i++) {
					String crtTerm_i = terms[i];
					for (int j = 0; j < terms.length; j++) {
						exporter.setCombinedComplexity(crtTerm_i, terms[j], cooccurrenceMatrix.get(i, j));
					}
				}
				
				exporter.close();
				LOG.info("done. took " + watch);
		}

		
		LOG.info("everything is ready in [" + new File(this.outDir).getAbsolutePath() + "]. Goodbye!");
	}


	private TermMatrixRW newExporter(String[] terms, HashMap<String,Long> variables, String originComment) throws IOException {
		String dirName = this.outDir;
		File dir = new File(dirName);
		if (!dir.exists())
				dir.mkdirs();

		return new TermMatrixRW(terms, variables, dir, terms.length, originComment);
	}

	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Options options = new Options();

		Option opt = Option.builder("h")
				.desc("print these help instructions")
				.longOpt("help")
				.hasArg(false)
				.build();
//		Option opt =  OptionBuilder
//				.withDescription("print these help instructions")
//				.withLongOpt("help")
//				.hasArg(false)
//				.create("h");
		options.addOption(opt);
		opt = Option.builder("i")
				.desc("lucene index")
				.longOpt("index")
				.argName("directory")
				.required()
				.hasArg()
				.build();
//		opt = OptionBuilder
//				.withArgName("directory")
//				.hasArg()
//				.isRequired()
//				.withDescription("lucene index")
//				.withLongOpt("index")
//				.create("i");
		options.addOption(opt);
		opt = Option.builder("n")
				.hasArg()
				.required()
				.argName("number")
				.desc("number of first terms to get statistics for, in decreasing frequency order")
				.longOpt("nrterms")
				.build();
//		opt = OptionBuilder
//				.withArgName("number")
//				.hasArg()
//				.isRequired()
//				.withDescription("number of first terms to get statistics for, in decreasing frequency order")
//				.withLongOpt("nrterms")
//				.create("n");
		options.addOption(opt);
		opt = Option.builder("o")
				.argName("directory")
				.hasArg()
				.required(false)
				.desc("directory containing resulting cooccurrences, NGD and UNGD matrices")
				.longOpt("outdir")
				.build();
//		opt = OptionBuilder
//				.withArgName("directory")
//				.hasArg()
//				.isRequired(false)
//				.withDescription("directory containing resulting cooccurrences, NGD and UNGD matrices")
//				.withLongOpt("outdir")
//				.create("o");
		options.addOption(opt);
		opt = Option.builder("f")
				.argName("number")
				.required(false)
				.desc("minimum frequency a term must have in order to be taken into account, default 20")
				.longOpt("minfreq")
				.build();
//		opt = OptionBuilder
//				.withArgName("number")
//				.hasArg()
//				.isRequired(false)
//				.withDescription("minimum frequency a term must have in order to be taken into account, default 20")
//				.withLongOpt("minfreq")
//				.create("f");
		options.addOption(opt);
		opt = Option.builder("d")
				.hasArg()
				.required(false)
				.argName("number")
				.desc("maximum number of documents to inspect")
				.longOpt("maxdocs")
				.build();
//		opt = OptionBuilder
//				.withArgName("number")
//				.hasArg()
//				.isRequired(false)
//				.withDescription("maximum number of documents to inspect")
//				.withLongOpt("maxdocs")
//				.create('d');
		options.addOption(opt);
//		opt = OptionBuilder
//				.withArgName("number")
//				.hasArg()
//				.isRequired(false)
//				.withDescription("nrLines in terms of the text window : two terms are counted as co-occurring if they are in the same window, default 20")
//				.withLongOpt("windowsize")
//				.create('w');
		opt = Option.builder("w")
				.longOpt("windowsize")
				.desc("nrLines in terms of the text window : two terms are counted as co-occurring if they are in the same window, default 20")
				.required(false)
				.hasArg()
				.argName("number")
				.build();
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
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ngd-calculator", options);
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public String getOutDir() {
		return outDir;
	}

	public void setMaxDocumentsToInspect(int maxDocumentsToInspect) {
		this.maxDocumentsToInspect = maxDocumentsToInspect;
	}
	
	static Logger LOG = Logger.getLogger(ExtractTermFrequenciesMatrixFromPositionalIndex.class);

}
