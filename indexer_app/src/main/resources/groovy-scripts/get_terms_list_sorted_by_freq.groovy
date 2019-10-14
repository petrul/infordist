import inform.dist.ngd.NgdCalculator
import com.thoughtworks.xstream.XStream
import org.apache.lucene.search.IndexSearcher
import inform.lucene.IndexUtil
import org.apache.commons.lang.*

//minFreq = 1 * 1000
minFreq = 10

if (args.size() < 1) { throw new IllegalArgumentException("must provide args : index location") }

idxLocation = args[0]

IndexSearcher searcher = new IndexSearcher(idxLocation)

termsSet = new IndexUtil(searcher.indexReader).getTermsOrderedByFreqDesc(minFreq)


//termsArr = []
//termsArr.addAll(termsSet.collect {it})

println ("total number of documents ${searcher.indexReader.maxDoc()}")
println("got ${termsSet.size()} terms with frequency > $minFreq") ;
new File("terms-" + RandomStringUtils.randomAlphabetic(5) + ".txt").withWriter("UTF-8") { writer ->
	writer.write ("#total number of documents ${searcher.indexReader.maxDoc()}\n")
	writer.write ("#got ${termsSet.nrLines()} terms with frequency > $minFreq\n") ;
	int counter = 0;
	termsSet.each {
		writer.write("${counter}\t${it.term}\t${it.freq}\n")
		counter++
	}
}
