package infordist.gg;

import java.io.Serializable;

/**
 * result bean to be received by the job splitter after the job is done.
 * 
 * @author dadi
 *
 */
public class CombinedComplexityJobResult implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	String mainTerm;
	String[] termsToCompare;
	int[] results;
	
	
	public CombinedComplexityJobResult(String mainTerm, String[] termsToCompare, int[] results) {
		this.mainTerm = mainTerm;
		this.termsToCompare = termsToCompare;
		this.results = results;
	}
	
	public String getMainTerm() {
		return mainTerm;
	}
	public void setMainTerm(String mainTerm) {
		this.mainTerm = mainTerm;
	}
	public String[] getTermsToCompare() {
		return termsToCompare;
	}
	public void setTermsToCompare(String[] termsToCompare) {
		this.termsToCompare = termsToCompare;
	}
	public int[] getResults() {
		return results;
	}
	public void setResults(int[] results) {
		this.results = results;
	}
}
