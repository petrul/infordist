package wiki.indexer.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.cli.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.tools.ant.taskdefs.BUnzip2;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wiki.indexer.IndexingTextProcessor;
import wiki.indexer.WikipediaDumpSaxParser;
import wiki.indexer.WikipediaSnowballAnalyzer;
import wiki.indexer.exception.MaxPagesReachedException;

/**
 * 
 */
public class WikipediaIndexer implements Runnable {

	String wikipediaXmlLocation;
	String indexLocation;
	boolean useShutDownHook = true;
	private Integer maxPages = null;

	public WikipediaIndexer(String wikipediaXmlLocation, String indexLocation) {
		this.setIndexLocation(indexLocation);
		this.setWikipediaXmlLocation(wikipediaXmlLocation);
		LOG.info("will index dump [" + wikipediaXmlLocation + "] to location [" + this.indexLocation + "]");
	}

	@Override
	public void run() {
		try {

			{
				File idx = new File(indexLocation);
				if (idx.exists()) {
					FileUtils.deleteDirectory(idx);
					LOG.warn("removing existing directory [" +idx + "]");
				}
				idx.mkdirs();

				File dump = new File(wikipediaXmlLocation);
				if (!dump.exists())
					throw new IllegalArgumentException("wikipedia dump file does not exist");
			}

			LOG.info("starting indexation of file [" + wikipediaXmlLocation + "] to index [" + indexLocation + "] ...");
			StopWatch watch = new StopWatch(); watch.start();
			
			final IndexWriter indexWriter = new IndexWriter(
					indexLocation, 
					new WikipediaSnowballAnalyzer("English"),
					MaxFieldLength.UNLIMITED);

			if (this.useShutDownHook)
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							try {
								IndexReader reader = IndexReader.open(indexLocation);
								if (reader.isOptimized())
									return;
								
								LOG.info("running shutdown hook, will optimize & close index...");
								
								indexWriter.optimize();
								indexWriter.close();
							} catch (AlreadyClosedException e) {
								// ok, so it was already closed.
							}
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			
//			Chunker chunker;
//			{
//				if (this.chunkBy == ChunkBy.SENTENCE)
//					chunker = new SentenceChunker();
//				else
//					chunker = new NoChunker();
//			}
			IndexingTextProcessor processor = new IndexingTextProcessor(indexWriter);
			WikipediaDumpSaxParser saxParser = new WikipediaDumpSaxParser(processor);
			if (this.maxPages != null)
				saxParser.setMaxPages(this.maxPages);
			
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setContentHandler(saxParser);
			try {
				InputStream is = new FileInputStream(new File(wikipediaXmlLocation));
				if (wikipediaXmlLocation.endsWith(".bz2")) {
					is = new BZip2CompressorInputStream(is);
				}
				xmlReader.parse(new InputSource(new BufferedInputStream(is)));
			} catch (MaxPagesReachedException e) {
				LOG.info("reached max pages threshold " + e.getNrPages() + ", will stop indexing");
			}

			indexWriter.optimize();
			indexWriter.close();

			LOG.info("done. took " + watch);
			
		}  catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		Options options = new Options();
		Option opt = Option.builder("h")
				.longOpt("help")
				.desc("print these help instructions")
				.hasArg(false)
				.build();
//		Option opt = OptionBuilder
//				        .withDescription("print these help instructions")
//				        .withLongOpt("help")
//				        .hasArg(false)
//				        .create("h");
		options.addOption(opt);
		opt = Option.builder("x")
				.argName("filename")
				.required()
				.desc("wikipedia xml dump")
				.build();
//		opt = OptionBuilder.withArgName("filename")
//				        .hasArg()
//				        .isRequired()
//				        .withDescription("wikipedia xml dump")
//				        .create("x");
		options.addOption(opt);
		opt = Option.builder("o")
				.argName("directory")
				.hasArg()
				.required(false)
				.desc("index output directory")
				.build();
//		opt = OptionBuilder.withArgName("directory")
//				        .hasArg()
//				        .isRequired(false)
//				        .withDescription("index output directory")
//				        .create("o");
		options.addOption(opt);
		opt = Option.builder("n")
				.longOpt("max-pages")
				.hasArg()
				.required(false)
				.desc("maximum wikipedia pages to process")
				.build();
//		opt = OptionBuilder
//						.withLongOpt("max-pages")
//				        .hasArg()
//				        .isRequired(false)
//				        .withDescription("maximum wikipedia pages to process")
//				        .create("n");
		options.addOption(opt);
//		opt = OptionBuilder
//						.withLongOpt("chunk")
//				        .hasArg()
//				        .isRequired(false)
//				        .withDescription("chunk text by: page|sentence. Default, page")
//				        .create("c");
//		options.addOption(opt);
		
		try {

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse( options, args);
			
			if (cmd.hasOption("help")) {
				printUsage(options);
				return;
			}
			
			String dump = cmd.getOptionValue("x");
			String out;
			if (cmd.hasOption("o"))
				out = cmd.getOptionValue("o");
			else
				out = "index-" + RandomStringUtils.randomAlphabetic(3);
			
			WikipediaIndexer wi = new WikipediaIndexer(dump, out);
			
			{
				if (cmd.hasOption("max-pages")) {
					int maxPages = Integer.parseInt(cmd.getOptionValue("max-pages"));
					LOG.info("maximum nr of pages to index : " + maxPages);
					wi.setMaxPages(maxPages);
				}
			}
			
//			{
//				if (cmd.hasOption("chunk")) {
//					String optionValue = cmd.getOptionValue("chunk");
//					if ("sentence".equalsIgnoreCase(optionValue.toLowerCase())) {
//						wi.chunkBy = ChunkBy.SENTENCE;
//					}
//					LOG.info("chunking by " + wi.chunkBy.toString().toLowerCase());
//				}
//			}

			wi.run();

		} catch (ParseException e) {
			System.err.println( "Arguments problem. " + e.getMessage() );
			printUsage(options);
		}
	}

	private void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	private static void printUsage(Options options ) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("wikipedia-indexer", options );
	}
	
	// accessors
	public String getWikipediaXmlLocation() {
		return wikipediaXmlLocation;
	}

	public void setWikipediaXmlLocation(String wikipediaXmlLocation) {
		this.wikipediaXmlLocation = wikipediaXmlLocation;
	}

	public String getIndexLocation() {
		return indexLocation;
	}

	public void setIndexLocation(String indexLocation) {
		this.indexLocation = indexLocation;
	}

	public void setUseShutDownHook(boolean useShutDownHook) {
		this.useShutDownHook = useShutDownHook;
	}

//	public void setChunkBy(ChunkBy chunkBy) {
//		this.chunkBy = chunkBy;
//	}

	final static Logger LOG = Logger.getLogger(WikipediaIndexer.class);

}
