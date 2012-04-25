package wiki.indexer;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;

public abstract class OptimizeIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String idxLocation = args[0];
		try {
			IndexWriter iw = new IndexWriter(idxLocation, new StandardAnalyzer(), MaxFieldLength.UNLIMITED);
			LOG.info("opened index " + idxLocation + " starting optimization...");
			StopWatch watch = new StopWatch();
			watch.start();
			
			iw.optimize();
			iw.close();
			
			watch.stop();
			LOG.info("done. took " + watch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static Logger LOG = Logger.getLogger(OptimizeIndex.class);
}
