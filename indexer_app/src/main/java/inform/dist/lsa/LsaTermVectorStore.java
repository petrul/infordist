package inform.dist.lsa;

import inform.dist.serialization.MatTextFileExporter;
import inform.dist.serialization.MatTextFileReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import matrix.store.NioFileMatrixStore;

public class LsaTermVectorStore {

	final static String TERMVECTORS_BIN = "termvectors.bin";
	final static String TERMS_MAT = "terms.mat";
	
	String[] terms;
	
	NioFileMatrixStore termVectors;
	
	Map<String, Integer> terms_id;
	
	
	/**
	 * creation constructor
	 * @param dir
	 * @param terms
	 */
	public LsaTermVectorStore(File dir, String[] terms, long[][] termVectorsMatrix) {
		if (dir.exists())
			throw new IllegalArgumentException("location [" + dir + "] exists, cannot create a new term vector");
		dir.mkdirs();
	
		MatTextFileExporter termsFile;
		
		this.terms = terms;
		int nterms = this.terms.length;
		int ndocs = termVectorsMatrix[0].length;
		
		this.termVectors = new NioFileMatrixStore(new File(dir, TERMVECTORS_BIN), nterms, ndocs, "rw");

		termsFile = new MatTextFileExporter(new File(dir, TERMS_MAT), "terms file");
		
		Assert.assertEquals(termVectorsMatrix.length, nterms);
		for (int i = 0; i < nterms; i++)
			for (int j = 0; j < ndocs; j++)
				this.termVectors.put(i, j, (int)termVectorsMatrix[i][j]);
		termsFile.writeStringArray("terms", terms, "terms");
		termsFile.writeScalar("total_docs", ndocs, "number of documents");
		termsFile.close();
		this.termVectors.close();
	}

	
	public LsaTermVectorStore(File dir) {
		MatTextFileReader termsFile = new MatTextFileReader(new File(dir, TERMS_MAT));
		this.terms = termsFile.getStringArray("terms");
		Long ndocs = termsFile.getLongScalar("total_docs");
		
		Assert.assertNotNull(ndocs);
		int nterms = this.terms.length;
		this.terms_id = new HashMap<String, Integer>(nterms);
		for (int i = 0; i < nterms; i++) {
			this.terms_id.put(terms[i], i);
		}

		
		this.termVectors = new NioFileMatrixStore(new File(dir, TERMVECTORS_BIN), this.terms.length, ndocs.intValue(), "r");
	}

	int[] getVector(String term) {
		Integer termid = this.terms_id.get(term);
		Assert.assertNotNull("no such term [" + term + "]", termid);
		return this.termVectors.copyOfRow(termid);
	}
}
