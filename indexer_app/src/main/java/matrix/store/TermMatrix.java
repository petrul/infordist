package matrix.store;

/**
 * A very needed data stucture. A square matrix indexable by strings and a vector indexable by
 * string. The matrix stores distances-like values between terms; the vector stores a value that
 * corresponds to the complexity a term.
 * 
 * <p>An example of use is storing complexities. Term complexities are stored in the vector. 
 * Term conditional complexities (K(x|y), combined complexities - K(x,y), or some other distance
 * are stored in the matrix.
 *  
 * @author dadi
 *
 */
public interface TermMatrix {
	
	void setCombinedComplexity(String term1, String term2, int value);
	
	void setComplexity(String term, int value);
	
	int getComplexity(String term);
	
	int getCombinedComplexity(String term1, String term2);
	
	int[] getCombinedComplexityRow(String term);
	
	/**
	 * return the integer index of this term
	 */
	int getTermIndex(String term);
	

	String[] getTerms();
	void setTerms(String[] terms);
	
	String getTerm(int index);
	
	/* this is metainformation that you may want to add to your term matrix*/
//	void setVariable(String name, String value);
//	void setVariable(String name, long value);
	
	long getVariable(String name);
	
	void close();

}
