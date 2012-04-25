package inform.dist.ngd;

import java.io.Serializable;

/**
 *  a Term with its distance  
 *
 */
public class WeightedTerm implements Comparable<WeightedTerm>, Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * id for this term; matrices use this id for their columns and rows, not the actual term
	 */
	int index;
	String 	term;
	double 	weight;
	
	public WeightedTerm(String t, double w) {
		this.term = t;
		this.weight = w;
		this.index = -1; // don't care
	}
	
	public WeightedTerm(String t, double w, int index) {
		this.term = t;
		this.weight = w;
		this.index = index;
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}


	/**
	 * so that natural ordering be descending (smallest weight first)
	 */
	@Override
	public int compareTo(WeightedTerm o) {
		double thatWeight = o.getWeight();
		if (this.weight < thatWeight) return -1;
		if (this.weight > thatWeight) return 1;
		if (this.weight == thatWeight)
			return this.term.compareTo(o.term);
		return 0;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return this.term + "(" + this.weight + ")";
	}
	
}
