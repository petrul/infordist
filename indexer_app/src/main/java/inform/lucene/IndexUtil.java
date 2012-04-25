package inform.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import wiki.indexer.TermAndFreq;

public class IndexUtil {
	
	IndexReader reader;
	public IndexUtil(IndexReader reader) {
		this.reader = reader;
	}

	public List<TermAndFreq> getTermsOrderedByFreqDesc() {
		return this.getTermsOrderedByFreqDesc(1);
	}
	
	/**
	 * @return all {@link Term}s in this index, ordered decreasingly by frequency. The first
	 * term is the most frequent
	 */
	public List<TermAndFreq> getTermsOrderedByFreqDesc(int minFreq) {

		try {
			TermEnum terms = reader.terms();
			TreeSet<TermAndFreq> termSet = newOrderedDescFreqTermSet();

			int counter = 0;
			while (terms.next()) {
				Term term = terms.term();
				int freq = this.reader.docFreq(term);
				if (freq >= minFreq) {
					TermAndFreq tf = new TermAndFreq(term.text(), freq);
					termSet.add(tf);
				}
				
				if (counter % 1000 == 0)
					if (LOG.isDebugEnabled()) LOG.debug("got " + counter + " terms...");
				counter++;
			}

			ArrayList<TermAndFreq> result = new ArrayList<TermAndFreq>();
			result.addAll(termSet);
			
			return result;
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
	}
	
	public TreeSet<TermAndFreq> newOrderedDescFreqTermSet() {
		// al terms sorted descendingly
		TreeSet<TermAndFreq> termSet = new TreeSet<TermAndFreq>(new Comparator<TermAndFreq>() {
			@Override
			public int compare(TermAndFreq o1, TermAndFreq o2) {
					int f1 = o1.getFreq();
					int f2 = o2.getFreq();
					
					if (f1 == f2)
						return o1.getTerm().compareTo(o2.getTerm());
					
					return -(f1 - f2);
			}
		});
		return termSet;
	}
	
	Logger LOG = Logger.getLogger(IndexUtil.class);
}
