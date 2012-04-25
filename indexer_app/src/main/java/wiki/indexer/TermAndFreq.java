package wiki.indexer;

/**
 * a terms and how many times it occurs
 * @author dadi
 */
public class TermAndFreq {
	
	String 	term;
	int		freq;
	
	public TermAndFreq(String term, int freq) {
		super();
		this.term = term;
		this.freq = freq;
	}

	/**
	 * copy constructor
	 */
	public TermAndFreq (TermAndFreq tf) {
		this.term = tf.term;
		this.freq = tf.freq;
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}


	@Override
	public String toString() {
		return this.term + "("  + this.freq + ")";
	}


}
