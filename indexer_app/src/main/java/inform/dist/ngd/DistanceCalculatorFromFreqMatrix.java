package inform.dist.ngd;

import matrix.store.TermMatrix;

/**
 * A wrapper for a {@link TermMatrix} which can calculate different distances,
 * given a matrix storing absolute and relative occurrence frequencies.
 * 
 * @author dadi
 * 
 */
public class DistanceCalculatorFromFreqMatrix {
	
	protected TermMatrix matrix;
	protected double ln_totalDocs;
	
	
	public DistanceCalculatorFromFreqMatrix(TermMatrix mtx) {
		this.matrix = mtx;
		long totalDocs = this.matrix.getVariable("total_docs");
		this.ln_totalDocs = Math.log(totalDocs);
	}
	
	public double getNgd(String term1, String term2) {
		int hits1 = this.matrix.getComplexity(term1);
		int hits2 = this.matrix.getComplexity(term2);
		int bothHits = this.matrix.getCombinedComplexity(term1, term2);
		double result = NgdCalculator.getNgdFromFreqs(hits1, hits2, bothHits, this.ln_totalDocs);
		return result;
	}
	
	public double getUngd(String term1, String term2) {
		int hits1 = this.matrix.getComplexity(term1);
		int hits2 = this.matrix.getComplexity(term2);
		int bothHits = this.matrix.getCombinedComplexity(term1, term2);
		double result = NgdCalculator.getUngdFromFreqs(hits1, hits2, bothHits);
		return result;
	}
	
	public double getConditionalComplexity(String term1, String term2) {
		int hits1 = this.matrix.getComplexity(term1);
		int bothHits = this.matrix.getCombinedComplexity(term1, term2);
		return NgdCalculator.getConditionalComplexityFromFreqs(hits1, bothHits);
	}

	public double getComplexity(String term1) {
		int hits1 = this.matrix.getComplexity(term1);
		return NgdCalculator.getComplexityFromFreq(hits1, this.ln_totalDocs);
	}
}
