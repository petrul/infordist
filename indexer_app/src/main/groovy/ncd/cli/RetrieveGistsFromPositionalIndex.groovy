package ncd.cli

import inform.dist.ncd.GistRetriever
import inform.dist.ncd.compressor.Bzip2Compressor
import inform.lucene.IndexUtil
import org.apache.lucene.index.IndexReader

import java.util.concurrent.Executors;

/*
 * from a positional index, retrieve gists for terms and store them as compressed files
 *
 */

class RetrieveGistsFromPositionalIndex {

    int minfreq = 200;
    int nThreads = 2;

    void _main(String[] args) throws Exception {
//	System.setProperty("javax.xml.parsers.DocumentBuilderFactory",  "net.sf.saxon.om.DocumentBuilderFactoryImpl")
        String indexLocation = args[0];
        nThreads = Integer.parseInt(args[1]);

        IndexReader index = IndexReader.open(indexLocation);
        def terms = new IndexUtil(index).getTermsOrderedByFreqDesc(minfreq)[0..60000]
        def gistDir = System.properties['user.home'] + "/gists.bz2"
        println "got ${terms.size()} terms, will store them in $gistDir...";
        def compressor = new Bzip2Compressor()
        //compressor = new GzipCompressor()
//		def cache = new FsBinaryGistDirectory(gistDir, compressor);
        GistRetriever calc = new GistRetriever(index, cache)
        //analyzer = new SnowballAnalyzer('English')

        def executorService = Executors.newFixedThreadPool(nThreads)

        def job = { termandfreq ->
            println "calculating string for ${termandfreq} ..."
//			if (!cache.hasGist(termandfreq.term)) { // useless, check is already done
            calc.getGist(termandfreq.term)
//			}
        }

        terms.each {
            def ajob = job.curry(it) as Runnable;
            executorService.execute(ajob)

            while (executorService.activeCount >= nThreads) {
                Thread.sleep(2 * 1000)
            }
        }
        executorService.shutdown();
    }

    static void main(String[] args) {
        new RetrieveGistsFromPositionalIndex()._main(args)
    }
}


//new nld.RetrieveGistsFromPositionalIndex()._main(args)