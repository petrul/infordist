package inform.dist.nld;

import static org.junit.Assert.assertEquals;
import inform.dist.Constants;
import inform.dist.nld.cache.Gist;
import inform.dist.nld.cache.GistDirectory;
import inform.dist.nld.cache.BinaryGist;
import inform.dist.nld.cache.StringListGist;
import inform.lucene.IndexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;

import wiki.indexer.TermAndFreq;

/**
 * Gets term gist from a lucene index. A gist is the word context in which a term appears.
 * 
 * @author dadi
 */
public class GistRetriever {
	
	IndexReader index;
	
	private GistDirectory cache;
	
	int range = 3;

	protected Map<String, Short> codeMapping;
	
	
	public GistRetriever(IndexReader indexReader, GistDirectory cache) {
		this.index = indexReader;
		this.cache = cache;
		List<TermAndFreq> termsOrderedByFreqDesc = new IndexUtil(indexReader).getTermsOrderedByFreqDesc(100);
		String[] terms = new String[60001];
		for (int i = 0; i < 60001; i++) {
			terms[i] = termsOrderedByFreqDesc.get(i).getTerm();
		}
		this.codeMapping = BinaryGist.termCodesMapping(terms);
	}
	
	
	/**
	 * the gist is the word neighbourhood in which a word appears
	 */
	public StringListGist getGistAsStringArray(String term) throws IOException {
		LOG.info("retrieving gist of " + term + " from index...");
		StopWatch watch = new StopWatch(); watch.start();
		Term t = new Term(Constants.FIELD_TEXT, term);
		
		// a structure that allows identification of docs where this term appears and positions
		TermPositions iterator = this.index.termPositions(t);
		
		// a list of all gists of all occurrences in all documents (result)
		List<String> gist = new ArrayList<String>();
		//StringBuilder gist = new StringBuilder();
		
		int counter_total_apparitions = 0;
		int counter_docs = 0;
		
		// for each document
		while (iterator.next()) {
			int docNr = iterator.doc();
			int freq = iterator.freq();
			
			counter_docs++;
			
			TermPositionVector tpv =  (TermPositionVector) this.index.getTermFreqVector(docNr, Constants.FIELD_TEXT);
			
			// where in the indexing of TermPositionVector.getTerms() does this term appear ?
			int idx = tpv.indexOf(term);
			
			// a list of word neighbourhoods: each neigbourhood is a map with key = distance from the main term and value, the term
			List<Map<Integer, String>> crtDocGist = new ArrayList<Map<Integer,String>>(freq);
			
			// the terms appeared in the current document on the following positions :
			int[] apparitionPositions = new int[freq];
			for (int i = 0; i < freq; i++) {
				apparitionPositions[i] = iterator.nextPosition();
				crtDocGist.add(new TreeMap<Integer, String>()); // initialization
			}
			
			// ok now take each term in the current document and see if we can place it around the main term in one of its neighbourhoods
			String[] terms = tpv.getTerms();
			assertEquals(term, terms[idx]);
			
			int nrterms = terms.length;
			for (int i = 0; i < terms.length; i++) {
				int[] pos = tpv.getTermPositions(i);
				
				// for each position in which the wannabe neighbour (n) occurs
				for (int p : pos) {
					// for each position where the main term (m) appears
					for (int m = 0 ; m < apparitionPositions.length; m ++) {
						int mainterm = apparitionPositions[m];
						int dist = p - mainterm ;
						
						if (Math.abs(dist) <= this.range && i < nrterms) {
							
							// ok, we found a neighbour !
							Map<Integer, String> neighbourhood = crtDocGist.get(m);
							
							if (LOG.isTraceEnabled()) LOG.trace("found neighbour " + terms[i] + "/" + p + " for " + terms[idx] + "/" + mainterm);
							
							// we don't want the actual term in its own gist
							String stringToRemember = dist == 0 ? "_" : terms[i];
							//if (dist != 0)
							neighbourhood.put(dist, stringToRemember);
							
						}
					}
				}
			}
			
			// finished, now put them together
			for (int main_pos = 0 ; main_pos < apparitionPositions.length; main_pos ++) {
			//for (int mainterm: apparitionPositions) {
//				int mainterm = apparitionPositions[main_pos];
				StringBuilder sb = new StringBuilder();
				Map<Integer, String> map = crtDocGist.get(main_pos);
				for (int i : map.keySet()) {
					String word = map.get(i);
					sb.append(word).append(' ');
//					gist.add(word);
				}
//				sb.append();
				//gist.append(sb.toString());
				gist.add(sb.toString());
				counter_total_apparitions ++;
			}
		}
		
		LOG.info("term " + term + " appeared " + counter_total_apparitions + " times in " + counter_docs + " docs, has a gist size in contexts of " + gist.size() + ", gist retrieval took " + watch);
		
		return new StringListGist(gist);
	}

	
	public Gist getGist(String term) {
		Gist cached = this.cache.getGist(term);
		if (cached != null)
			return cached;

		List<String> gistAsStringArray;
		try {
			gistAsStringArray = this.getGistAsStringArray(term).getStringList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		StringBuilder sb = new StringBuilder();
		for (String s : gistAsStringArray) {
			sb.append(s);
			sb.append(Constants.GIST_CONTEXT_SEPARATOR);
		}
		String result = sb.toString();
		
		BinaryGist binaryGist = new BinaryGist(new StringListGist(result), this.codeMapping, 10 * Constants.BZIP2_BLOCK_SIZE);
		
		this.cache.storeGist(term, binaryGist);
		return binaryGist;
	}
	
//	/**
//	 * i had an idea: suppose i take the gist of a term and then i sort the terms so that gzip works better. It didn't work. 
//	 */
//	public OrderedGist getOrderedGist(String term) {
//		String result;
//		try {
//			result = this.processGistForBetterCompression(this.getGistAsStringArray(term).getStringList());
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		
//		return new OrderedGist(new StringReader(result));
//	}

//	/**
//	 * this should be moved in {@link OrderedGist#combine(inform.dist.nld.cache.Gist)}
//	 */
//	private String processGistForBetterCompression(List<String> gist) {
//		
//		Collections.sort(gist);
//		StringBuilder sb = new StringBuilder();
//		int gistsize = gist.size();
//		for (int i = 0; i < gistsize; i++) {
//			String elem = gist.get(i);
//			if (i > 0 && i < gistsize - 1) {
//				String before = gist.get(i - 1);
//				String after = gist.get(i + 1);
//
//				if (!before.equals(elem) && !after.equals(elem)) // element is unique, skip it, don't like noise
//					continue;
//			}
//			sb.append(elem).append(Constants.GIST_CONTEXT_SEPARATOR);
//		}
//		String result = sb.toString();
//		
//		return result;
//	}
	
	static Logger LOG = Logger.getLogger(GistRetriever.class);
}
