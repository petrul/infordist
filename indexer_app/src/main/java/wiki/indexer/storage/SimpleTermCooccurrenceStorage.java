package wiki.indexer.storage;

import inform.dist.ngd.NgdCalculator;
import inform.dist.util.UpperSymmetricalZeroDiagMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;


/**
 * Uses an in-memory matrix in order to store co-occurrence information. It stores a map <term, idx>
 * and a matrix. If you want the cooccurrence of term x and y, you take the indexes from the map and
 * then look the cell up in the matrix. 
 */
public class SimpleTermCooccurrenceStorage implements TermCoccurrenceStorage {
	
	int[] termAbsoluteFreq;
	UpperSymmetricalZeroDiagMatrix cooccurrenceMatrix;
	
	/**
	 * the values are the terms we are interested in. We restrain the "interesting" terms
	 * in order for the distance matrix to fit into the memory.
	 */
	Map<String, Integer> allowedTerms;
	
	public SimpleTermCooccurrenceStorage(Map<String, Integer> allowedTerms) {
		this.allowedTerms = allowedTerms;
		
		int nterms = allowedTerms.size();
		this.cooccurrenceMatrix = new UpperSymmetricalZeroDiagMatrix(nterms);// int[nterms][nterms];
		this.termAbsoluteFreq = new int[nterms];
	}
	
	
	@Override
	public void increaseTermFreq(String crtTerm) {
		Integer idx = this.allowedTerms.get(crtTerm);
		if (idx == null)
			return;
		
		this.termAbsoluteFreq[idx] ++;
	}

	
	@Override
	public void markCooccurrence(String term1, String term2) {
		Integer idx1 = this.allowedTerms.get(term1);
		if (idx1 == null) return;
		Integer idx2 = this.allowedTerms.get(term2);
		if (idx2 == null) return;
		
		int value = this.cooccurrenceMatrix.get(idx1, idx2);
		value++;
		this.cooccurrenceMatrix.set(idx1,idx2,value);
	}


	/**
	 * @param i the index of the first term
	 * @param j the index of the second term
	 * @param ln_totalNrOfPages the natural logarithm of the total nr of pages
	 */
	public double getNgd(int i, int j, double ln_totalNrOfPages) {
		int hits1 = this.termAbsoluteFreq[i];
		int hits2 = this.termAbsoluteFreq[j];
		int hitsBoth = this.cooccurrenceMatrix.get(i,j);
		double ngd = NgdCalculator.getNgdFromFreqs(hits1, hits2, hitsBoth, ln_totalNrOfPages);
		return ngd;
	}
	
	/**
	 * "unnormalized" google distance : @see {@link NgdCalculator#getUngdFromFreqs(int, int, int)}
	 */
	public double getUngd(int i, int j) {
		int hits1 = this.termAbsoluteFreq[i];
		int hits2 = this.termAbsoluteFreq[j];
		int hitsBoth = this.cooccurrenceMatrix.get(i,j);
		double ngd = NgdCalculator.getUngdFromFreqs(hits1, hits2, hitsBoth);
		return ngd;
	}
	
	/**
	 * @return G(i|j) = G(i,j) - G(j) = log hits j - log hits(i,j)
	 */
	public double getConditionalComplexity(int i, int j) {
		int hits_j = this.termAbsoluteFreq[j];
		int hits_both = this.cooccurrenceMatrix.get(i,j);
		double condcompl = NgdCalculator.getConditionalComplexityFromFreqs(hits_j, hits_both);
		return condcompl;
	}
	
	/**
	 * G(y|x)/G(y) = G(x,y)/G(y) - 1 = (log M - log f_xy) / (log M - log f_y) - 1
	 * 
	 * division by G(y), *NOT* by G(x). Why? Because, x is _given_, what varies is y. 
	 * Obvious y's are very simple ones.
	 * 
	 * @param y calculate the conditional complexity of y
	 * @param x ... given x
	 * 
	 * @see NgdCalculator#getNormalizedConditionalComplexityFromFreqs(int, int, double)
	 */
	public double getNormalizedConditionalComplexity(int y, int x, double ln_totalNrOfPages) {
		int hits_y = this.termAbsoluteFreq[y];
		int hits_both = this.cooccurrenceMatrix.get(y,x);
		double normcondcompl = NgdCalculator.getNormalizedConditionalComplexityFromFreqs(hits_y, hits_both, ln_totalNrOfPages);
		return normcondcompl;
	}
	
	public void exportNgdMatrixToCsv(Writer outFile, long totalNumberOfPages) {
		PrintWriter writer = new PrintWriter(outFile);
		
		int nterms = this.termAbsoluteFreq.length;
		double log_totalNrOfPages = Math.log(totalNumberOfPages);
		
		for (int i = 0; i < nterms; i++) {
			int hits1 = this.termAbsoluteFreq[i];
			if (hits1 > totalNumberOfPages)
				LOG.warn("for hits(" + i + ") = " + hits1 + " > total hits " + totalNumberOfPages );
			
			for (int j = 0; j < nterms; j++) {
				double ngd;
				if (i == j)
					ngd = 0.0;
				else {
					int hits2 = this.termAbsoluteFreq[j];
					int hitsBoth = this.cooccurrenceMatrix.get(i,j);
					
					ngd = NgdCalculator.getNgdFromFreqs(hits1, hits2, hitsBoth, log_totalNrOfPages);
				}
				writer.print(ngd);
				writer.print(";");
			}
			writer.println();
		}
	}
	
	public UpperSymmetricalZeroDiagMatrix getCooccurrenceMatrix() {
		return cooccurrenceMatrix;
	}

	public void setCooccurrenceMatrix(UpperSymmetricalZeroDiagMatrix cooccurrenceMatrix) {
		this.cooccurrenceMatrix = cooccurrenceMatrix;
	}
	
	public int[] getTermAbsoluteFreq() {
		return termAbsoluteFreq;
	}

	public void setTermAbsoluteFreq(int[] termAbsoluteFreq) {
		this.termAbsoluteFreq = termAbsoluteFreq;
	}

	
	/**
	 * the termlist is a text file of the following format : idx <tab> term <tab> freq
	 */
	@Deprecated
	public SimpleTermCooccurrenceStorage(File termList, int nrTerms) {
		try {
			this.allowedTerms = new HashMap<String, Integer>(nrTerms);
			LineNumberReader fileReader = new LineNumberReader(new FileReader(termList));
			
			String crtLine;
			while (
					((crtLine = fileReader.readLine()) != null)
					&& (allowedTerms.size() < nrTerms)
					
			) {
				if (crtLine.startsWith("#")) {
					continue;
				}
					
				String[] split = crtLine.split("\\s+");
				String term = split[1];
				int idx = Integer.parseInt(split[0]);
				Integer existing = allowedTerms.put(term, idx);
				if (existing != null)
					LOG.warn("already had term [" + term + "]");
				
			}
			
			Assert.assertEquals("expected to have " + nrTerms + " allowed terms", nrTerms, allowedTerms.size());
			
			int nterms = allowedTerms.size();
			this.cooccurrenceMatrix = new UpperSymmetricalZeroDiagMatrix(nterms);// int[nterms][nterms];
			this.termAbsoluteFreq = new int[nterms];
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	static Logger LOG = Logger.getLogger(SimpleTermCooccurrenceStorage.class);


}
