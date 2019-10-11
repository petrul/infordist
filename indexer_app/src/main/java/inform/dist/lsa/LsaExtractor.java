package inform.dist.lsa;

import inform.dist.Constants;
import inform.dist.MatrixAccessor;
import inform.dist.serialization.MatExporter;
import inform.dist.serialization.MatTextFileExporter;
import inform.dist.util.Util;
import inform.lucene.IndexUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import matrix.store.NioFileMatrixStore;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.search.IndexSearcher;

import wiki.indexer.TermAndFreq;

/**
 * extracts the raw LSA matrix from an index
 * 
 * @author dadi
 */
public class LsaExtractor implements Runnable {

	private static final int DEFAULT_WINDOW_SIZE = 1000;
	IndexSearcher 	searcher;
	int 			nrTerms;
	
	/* a document is split into text window sizes ; for each window there will be generated a random vector */
	int windowSize = DEFAULT_WINDOW_SIZE; // nr of terms per window
	
	/* the size of a term vector, random vector */
	int termVectorLength = 1800;
	
	int nrRandomNonZeroEntries = 8;
	
	int minFreq = 20;
	private String outfile;
	
	int maxDocumentsToInspect = Integer.MAX_VALUE;

	
	@Override
	public void run() {
		try {
			final IndexReader idxReader = searcher.getIndexReader();
			List<TermAndFreq> termList = new IndexUtil(idxReader).getTermsOrderedByFreqDesc(this.minFreq);
			
			/* a bidirectional map so we can get a terms's index easily */
			Map<String, Integer> term_id_map = Util.get_term_id_map(termList, Math.min(termList.size(),this.nrTerms));


			/* make LSA matrix */
			int totalTerms = term_id_map.size();
			int totalDocs = idxReader.maxDoc();
			
			LOG.info("a total of " + totalDocs + " documents reported by the index. will use a text window size of " + this.windowSize);
			
			if (totalTerms < this.nrTerms) {
				int totalNumberOfDocumentInThisFreakingIndex = 0;
				TermEnum termEnum = idxReader.terms();
				while (termEnum.next()) {
					totalNumberOfDocumentInThisFreakingIndex++;
				}
				LOG.warn("only managed to get " + totalTerms + " terms with freq > " + this.minFreq + " from the total " + totalNumberOfDocumentInThisFreakingIndex);
			} else
				LOG.info("will do LSA for " + totalTerms + " terms");

			/* this is the final matrix containing term vectors */
			final long[][] termVectors = new long[totalTerms][this.termVectorLength];
			
			
			int counterNonEmptyDocs = 0;
			for (int i = 0; i < Math.min(totalDocs, this.maxDocumentsToInspect); i++) {
				
				RandomVectorGeneratorMap randomVectorMap = new RandomVectorGeneratorMap(this.termVectorLength, this.nrRandomNonZeroEntries);
				
				TermPositionVector termVector = (TermPositionVector) idxReader.getTermFreqVector(i, Constants.FIELD_TEXT);
				if (termVector == null) {
					if (LOG.isDebugEnabled()) {
						Document doc = idxReader.document(i);
						LOG.debug("no term vector for document " + i + ": " + doc.getField("id").stringValue());
					}
					continue;
				}
				counterNonEmptyDocs ++;

				if (counterNonEmptyDocs % 10000 == 0)
					System.out.print("" + counterNonEmptyDocs + "... ");
				
				String[] terms = termVector.getTerms();
				
				for (int j = 0; j < terms.length; j++) {
					String crtTerm = terms[j];
					if (!term_id_map.containsKey(crtTerm))
						continue;
					int crtTermId = (Integer) term_id_map.get(crtTerm);
					
					int[] positions = termVector.getTermPositions(j);
					if (positions == null)
						throw new IllegalStateException("no term positions have been stored for index. this is obnoxious, to say the least, in my humble opinion so i will stop.");
					
					long[] termRow = termVectors[crtTermId];
					for (int k = 0; k < positions.length; k++) {
						int crtWindowId = k / this.windowSize;
						int[] docVector = randomVectorMap.get(crtWindowId);
						
						for (int l = 0; l < this.termVectorLength; l++)
							termRow[l] += docVector[l];
					}
				}
			}
			
			LOG.info("used " + counterNonEmptyDocs + " non-empty index documents from a total of " + idxReader.maxDoc());
			
//			{
//				LOG.info("calculation done, will serialize to [" + new File(this.outfile).getAbsolutePath() + "]...");
//				MatBinaryExporter<Double> ser 
//					= new MatBinaryExporter<Double>(this.outfile);
//				MatrixAccessor<Double> mtx_accessor = new MatrixAccessor<Double>() {
//	
//					@Override
//					public Double getCell(int i, int j) {
//						// natural logarithm
//						return Math.log(termVectors[i][j]);
//					}
//	
//					@Override
//					public int getColumns() {
//						return termVectors[0].length;
//					}
//	
//					@Override
//					public int getRows() {
//						return termVectors.length;
//					}
//				};
//				
//				ser.write(new TermsAndMatrix<Double>(termList, mtx_accessor));
//				LOG.info("serializing term vectors to human readable format...");
//				MatExporter mat = new MatTextFileExporter(new File(this.outfile + ".mat"), "LSA random indexing term vectors");
//				mat.writeStringArray("terms", this.getTermArray(termList, totalTerms), "most frequent terms");
//				mat.writeMatrix("term-vectors", mtx_accessor, "lsa term vectors");
//			}
			
			{
				LOG.info("serializing cosine distance to human readable format...");
				MatrixAccessor<Double> distances = new MatrixAccessor<Double>() {

					@Override
					public Double getCell(int i, int j) {
						return cosine(termVectors[i], termVectors[j]);
					}

					@Override
					public int getColumns() {
						return termVectors.length;
					}

					@Override
					public int getRows() {
						return termVectors.length;
					}
					
				};
				
				
				MatExporter matfile = new MatTextFileExporter(new File(this.outfile + ".lsa-terms.mat"), "lsa terms");
				matfile.writeStringArray("terms", this.getTermArray(termList, totalTerms) , "list of terms");
				//termVectors = new NioFileMatrixStore(null, totalDocs, counterNonEmptyDocs, outfile)
				//matfile.writeMatrix("lsa-cosines", distances, "lsa cosines");
			}
			
		} catch (Exception e1) {
			if (e1 instanceof RuntimeException) throw (RuntimeException)e1;
			throw new RuntimeException(e1);
		}
	}

	private String[] getTermArray(List<TermAndFreq> termList, int totalTerms) {
		String[] result = new String[totalTerms];
		for (int i = 0; i < totalTerms; i++) {
			result[i] = termList.get(i).getTerm();
		}
		return result;
	}
	
	
	/**
	 * @return the cosine distance between the two vectors
	 */
	public static double cosine(long[] x, long[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException("the two vectors should have the same size");

		double scalarProduct = 0.0;
		double norm_x = 0.0;
		double norm_y = 0.0;

		for (int i = 0; i < x.length; i++) {
			long x_i = x[i];
			long y_i = y[i];

			scalarProduct += x_i * y_i;
			norm_x += x_i * x_i;
			norm_y += y_i * y_i;
		}

		norm_x = Math.sqrt(norm_x);
		norm_y = Math.sqrt(norm_y);

		double result = scalarProduct / (norm_x * norm_y);
		return result;
	}


	public LsaExtractor(String indexLocation, int nrTerms, int minFreq, int windowSize, String matFile) {
		try {
			this.searcher = new IndexSearcher(IndexReader.open(new File(indexLocation)));
			this.nrTerms = nrTerms;
			this.minFreq = minFreq;
			this.outfile = matFile;
			this.windowSize = windowSize;
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

			int n = Integer.parseInt(cmd.getOptionValue("nrterms"));
			int minfreq = 20;
			if (cmd.hasOption("minfreq"))
				minfreq = Integer.parseInt(cmd.getOptionValue("minfreq"));
			LOG.info("minimum frequency: " + minfreq);
			String outfile = cmd.getOptionValue("outfile");
			if (outfile == null)
				outfile = "lsa-term-vectors";
			
			int windowSize = DEFAULT_WINDOW_SIZE;
			if (cmd.hasOption("windowsize"))
				windowSize = Integer.parseInt(cmd.getOptionValue("windowsize"));
			LOG.info("using a text window size of " + windowSize);
			
			LsaExtractor lsa = new LsaExtractor(cmd.getOptionValue("index"), n, minfreq, windowSize, outfile);
			
			if (cmd.hasOption("maxdocs")) {
				int maxdocs = Integer.parseInt(cmd.getOptionValue("maxdocs"));
				LOG.info("will limit document inspection to " + maxdocs);
				lsa.setMaxDocumentsToInspect(maxdocs);
			}
			
			lsa.run();

		} catch (ParseException e) {
			System.err.println("Arguments problem. " + e.getMessage());
			printUsage(options);
		}
	}


	@SuppressWarnings("static-access")
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
				.withArgName("filename")
				.hasArg()
				.isRequired(false)
				.withDescription("resulting LSA term vector filename")
				.withLongOpt("outfile")
				.create("o");
		options.addOption(opt);
		opt = OptionBuilder
				.withArgName("number")
				.hasArg()
				.isRequired(false)
				.withDescription("minimum frequency a term must have in order to be taken into account")
				.withLongOpt("minfreq")
				.create("f");
		options.addOption(opt);
		opt = OptionBuilder
				.withArgName("number")
				.hasArg()
				.isRequired(false)
				.withDescription("maximum number of documents to inspect")
				.withLongOpt("maxdocs")
				.create("d");
		opt = OptionBuilder
				.withArgName("number")
				.hasArg()
				.isRequired(false)
				.withDescription("text window size in words, default " + DEFAULT_WINDOW_SIZE)
				.withLongOpt("windowsize")
				.create("w");
		options.addOption(opt);

		return options;
	}

	private static void printUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("lsa-extractor", options);
	}

	public void setMaxDocumentsToInspect(int maxPages) {
		this.maxDocumentsToInspect = maxPages;
	}
	
	static Logger LOG = Logger.getLogger(LsaExtractor.class);

}
