import org.apache.lucene.index.*
import inform.lucene.*

minfreq = 200

reader = IndexReader.open(args[0])
println "opened reader ${args[0]} with ${reader.numDocs()} documents and getting terms with minfreq=$minfreq";
terms = new IndexUtil(reader).getTermsOrderedByFreqDesc(minfreq);

max_freq = Integer.MIN_VALUE
min_freq = Integer.MAX_VALUE
first45k = terms[0..45000]
terms = first45k
terms.each {
	if (it.freq > max_freq) max_freq = it.freq ;
	if (it.freq < min_freq) min_freq = it.freq ;
}
println "got ${terms.size()} terms, min freq : ${min_freq}, max freq : ${max_freq}"

int nr_units = 100
//unit_size = (max_freq - min_freq) / nr_units
int unit_size = 100 * 100 / nr_units
int[] distribution = new int[nr_units]
int unfit_counter = 0
terms.each {
	int freq = it.freq
	int idx = freq / unit_size
	if (idx < nr_units)
		distribution[idx] = distribution[idx] + 1
	else
		unfit_counter++;
}
println "distribution : $distribution, with $unfit_counter variables that don't fit"

println "=" * 20
distribution.eachWithIndex {val, idx ->
	println "${idx * unit_size} ; $val"
}