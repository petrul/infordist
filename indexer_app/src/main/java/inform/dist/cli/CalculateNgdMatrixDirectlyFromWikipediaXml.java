package inform.dist.cli;

import inform.dist.MatrixAccessor;
import inform.dist.serialization.MatExporter;
import inform.dist.serialization.MatTextFileExporter;
import inform.dist.util.UpperSymmetricalZeroDiagMatrix;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wiki.indexer.FrequencyStoringTextProcessor;
import wiki.indexer.TextProcessor;
import wiki.indexer.WikipediaDumpSaxParser;
import wiki.indexer.WikipediaSnowballAnalyzer;
import wiki.indexer.exception.MaxPagesReachedException;
import wiki.indexer.storage.SimpleTermCooccurrenceStorage;

/**
 * runs the {@link WikipediaSnowballAnalyzer} with a special {@link TextProcessor} which builds a coccurence matrix
 * and finally the NGD matrix.
 * @deprecated use {@link ExtractTermFrequenciesMatrixFromPositionalIndex}
 */
public class CalculateNgdMatrixDirectlyFromWikipediaXml implements Runnable {

	private String 	termListFilename;
	private int 	nrTerms;
	private String 	wikipediaDumpFilename;
	private long 	wikipediaPageLimiter;
	

	public CalculateNgdMatrixDirectlyFromWikipediaXml(String wikipediaDumpFilename, long wikipediaPageLimiter, String termListFilename, int nrterms) {
		this.wikipediaDumpFilename = wikipediaDumpFilename;
		this.wikipediaPageLimiter = wikipediaPageLimiter;
		this.termListFilename = termListFilename;
		this.nrTerms = nrterms;
	}

	public static void main(String[] args) {
		if (args.length < 4)
			throw new IllegalArgumentException("arguments needed : " +
					"\n\t* wikipedia dump file, " +
					"\n\t* wikipedia page limiter (number or 'all'), " +
					"\n\t* term list file, " +
					"\n\t* number of terms to compare"
					);

		long limiter;
		String s_wikipagelimiter = args[1];
		{
			if ("all".equalsIgnoreCase(s_wikipagelimiter))
				limiter = Long.MAX_VALUE;
			else
				limiter = Long.parseLong(s_wikipagelimiter);
		}
		new CalculateNgdMatrixDirectlyFromWikipediaXml(args[0], limiter, args[2], Integer.parseInt(args[3])).run();
	}

	@Override
	public void run()  {
		try {
			Analyzer analyzer = new WikipediaSnowballAnalyzer("English");
			File allowedTerms = new File(this.termListFilename);
			LOG.info("using " + this.nrTerms + " terms from file " + this.termListFilename);
			
			SimpleTermCooccurrenceStorage storage = new SimpleTermCooccurrenceStorage(allowedTerms, this.nrTerms);
			
			LOG.info("initialized matrix storage, will start parsing " + this.wikipediaDumpFilename + " ...");
			StopWatch watch = new StopWatch(); watch.start();
			TextProcessor textProcessor = new FrequencyStoringTextProcessor(analyzer, storage);
			
			WikipediaDumpSaxParser saxParser = new WikipediaDumpSaxParser(textProcessor);
			saxParser.setMaxPages(this.wikipediaPageLimiter);
			
			InputStream wikipedia = new BufferedInputStream(new FileInputStream(new File(this.wikipediaDumpFilename)));
			
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(saxParser);
			
			try {
				xmlReader.parse(new InputSource(wikipedia));
			} catch (MaxPagesReachedException e) {
				LOG.info("reached maximum number of wikipedia pages " + e.getNrPages() + ", will stop parsing.");
			}
			
			long nPages = saxParser.getTextWindowCounter();
			LOG.info("processed " + nPages + " text documents (paragraphs). took " + watch);
			this.storeResults(storage, nPages);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void storeResults(final SimpleTermCooccurrenceStorage storage, long nPages) throws IOException 	{
		
		String randomString = new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date()) + "_" + RandomStringUtils.randomAlphabetic(5);
		String dirName = "ngd-semantics-" + randomString;
		File dir = new File(dirName);
		dir.mkdirs();
		
		StopWatch watch = new StopWatch();
		
		String originComment = "created from " +
				"wikipedia dump [" + new File(this.wikipediaDumpFilename).getAbsolutePath() + "]," +
				"term list [" + new File(this.termListFilename).getAbsolutePath() + "]"
				;  
		
		{
			File cooccMatrixFile = new File(dir, "cooccurrences.mat");
			Writer writer = new BufferedWriter(new FileWriter(cooccMatrixFile));
			LOG.info("writing coccurrence matrix to file [" + cooccMatrixFile.getAbsolutePath() + "]...");
			watch.reset(); watch.start();
			final UpperSymmetricalZeroDiagMatrix cooccurrenceMatrix = storage.getCooccurrenceMatrix();

			MatExporter exporter = new MatTextFileExporter(writer, originComment);
			
			exporter.writeScalar("total_docs", 
					nPages, 
					"total number of lucene docs, needed for ngd calculation");
			
			{
				MatrixAccessor<Integer> absoluteFreqVector = new MatrixAccessor<Integer>() {
					@Override
						public Integer getCell(int i, int j) {
							if (i > 0)
								throw new IllegalArgumentException("ca va pas non, c'est un vecteur!");
							return storage.getTermAbsoluteFreq()[j];
						}

					@Override
					public int getColumns() {
						 return storage.getTermAbsoluteFreq().length;
					}

					@Override
					public int getRows() {
						return 1;
					}	
				};
				
				exporter.writeMatrix(dirName, absoluteFreqVector, "absolute frequency of terms ");
			}
			
			{
				MatrixAccessor<Integer> matrix = new MatrixAccessor<Integer>() {
					@Override
					public Integer getCell(int i, int j) {
						return cooccurrenceMatrix.get(i, j);
					}

					@Override
					public int getColumns() {
						return cooccurrenceMatrix.getSize();
					}

					@Override
					public int getRows() {
						return cooccurrenceMatrix.getSize();
					}
				};
				
				exporter.writeMatrix("cooccurrences", 
						matrix,
						"a cell is the number of cooccurrences (in a common window of text) of terms i and j; " +
						"this is a symmetrical matrix with zeroes on the diagonal; source: english wikipedia"
						);
			}
			writer.close();
			LOG.info("done. took " + watch);
		}
		
		{
			File ngdMatrixFile = new File(dir, "ngd.mat");
			Writer writer = new BufferedWriter(new FileWriter(ngdMatrixFile));
			MatExporter exporter = new MatTextFileExporter(writer, originComment);
			final double ln_totaldocs = Math.log(nPages);
			MatrixAccessor<Double> matrix = new MatrixAccessor<Double>() {
				@Override
				public Double getCell(int i, int j) {
					return storage.getNgd(i, j, ln_totaldocs);
				}

				@Override
				public int getColumns() {
					return nrTerms;
				}

				@Override
				public int getRows() {
					return nrTerms;
				}
			};
			
			LOG.info("writing NGD matrix to file [" + ngdMatrixFile.getAbsolutePath() + "]...");
			watch.reset(); watch.start();
			exporter.writeMatrix("ngd", matrix, "normalized google distances");
			writer.close();
			LOG.info("done. took " + watch);
		}
		
		
		{
			File ngdMatrixFile = new File(dir, "ungd.mat");
			Writer writer = new BufferedWriter(new FileWriter(ngdMatrixFile));
			MatExporter exporter = new MatTextFileExporter(writer, originComment);
			watch.reset(); watch.start();
			MatrixAccessor<Double> matrix = new MatrixAccessor<Double>() {
				@Override
				public Double getCell(int i, int j) {
					return storage.getUngd(i, j);
				}

				@Override
				public int getColumns() {
					return nrTerms;
				}

				@Override
				public int getRows() {
					return nrTerms;
				}
			};
			
			LOG.info("writing UnGD matrix to file [" + ngdMatrixFile.getAbsolutePath() + "]...");
			exporter.writeMatrix("ungd", matrix, "unnormalized google distances");
			writer.close();
			
			LOG.info("done. took " + watch);
			LOG.info("everything is ready in [" + dir.getAbsolutePath() + "]. Goodbye!");
		}
	}


	static Logger LOG = Logger.getLogger(CalculateNgdMatrixDirectlyFromWikipediaXml.class);
}
