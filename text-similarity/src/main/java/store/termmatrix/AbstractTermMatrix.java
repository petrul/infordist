package store.termmatrix;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import store.matlabfile.MatTextFileExporter;
import store.matlabfile.MatTextFileReader;
import store.matrix.IntMatrixStore;

/**
 * a term complexity matrix which is backed by a directory containing three files:
 * - the terms
 * - the complexities
 * - the combined complexities
 * 
 * @author dadi
 *
 */
public abstract class AbstractTermMatrix implements TermMatrix {

	private static final String TERMS_TXT = "terms.matlabfile";
	private static final String COMBINED_COMPLEXITIES_BINMAT = "combined-complexities.binarymatrix";
	private static final String COMPLEXITIES_BINMAT = "complexities.binarymatrix";

	String[] terms;

	Map<String, Integer> terms_id;
	
	IntMatrixStore cStore;
	IntMatrixStore ccStore;
	
	/* records if this matrix was already on disk or was just created */
	enum OpeningStatus { NEW, EXISTING }
	public OpeningStatus openingStatus;
	
	/* private */
	private 	File 	baseDir;
	private 	int 	nterms;
	MatTextFileExporter exporter;
	Map<String, Object> 	variables = new HashMap<String, Object>();

	public AbstractTermMatrix() {}
	
	public AbstractTermMatrix(String[] terms, File dir, String originalComment) {
		this(terms, dir, 5, originalComment);
	}
	
	/**
	 * constructor for the creation of a new directory
	 */
	public AbstractTermMatrix(String[] terms, File dir, int cacheSize, String originalComment) {
		init(terms, dir, cacheSize, originalComment);
	}

	protected void init(String[] terms, File dir, int cacheSize, String originalComment) {
		this.baseDir = dir;
		this.setTerms(terms);
		
		if (dir.exists() && dir.list().length > 0) { // dir exists and non-empty
			if (terms != null) {
				LOG.warning("directory ["
								+ dir
								+ "] exists, will load what is in this directory, you'd better have provided the same terms or I'll throw exception");

				this.initFromExisting(dir, cacheSize);
				if (this.terms_id.size() != terms.length)
					throw new IllegalArgumentException("not the same number of terms, constructor says " + terms.length
							+ " stored matrix says " + this.terms_id.size());
				for (int i = 0; i < terms.length; i++) {
					if (this.terms_id.get(terms[i]) != i)
						throw new IllegalArgumentException("term " + terms[i] + " was expected to be on position " + i
								+ ", it is instead on position " + this.terms_id.get(terms[i]));
				}
				this.openingStatus = OpeningStatus.EXISTING;
				LOG.info("existing matrix loaded : " + terms.length  + " terms at [" + dir + "]");
			} else {
				LOG.warning("directory [" + dir + "] exists, will load what is in this directory");
				this.initFromExisting(dir, cacheSize);
				this.openingStatus = OpeningStatus.EXISTING;
				LOG.info("existing matrix loaded : " + this.nterms  + " terms at [" + dir + "]");
			}
		} else {
			// really create the matrix
			LOG.info("creating the term matrix at location " + dir + ", it may take a while...");
			dir.mkdirs();
			
			File termTextFile = new File(this.baseDir, TERMS_TXT);
			this.exporter = new MatTextFileExporter(termTextFile, originalComment);
			exporter.writeStringArray("terms", terms, "terms");
			
			
			{
				File complexitiesFile = new File(dir, COMPLEXITIES_BINMAT);
				assertTrue(this.nterms > 0); // it should have been initialized be now
				cStore =  this.newIntMatrixStore(complexitiesFile, 1, this.nterms, 1);
				cStore.init(-1);
				cStore.flush();
			}
			
			{
				File ccomplexitiesFile = new File(dir, COMBINED_COMPLEXITIES_BINMAT);
				ccStore = this.newIntMatrixStore(ccomplexitiesFile, this.nterms, this.nterms, cacheSize);
				ccStore.init(-1);
				ccStore.flush();
			}
			this.openingStatus = OpeningStatus.NEW;
			LOG.info("done creating the term matrix.");
		}

	}

    private void assertTrue(boolean b) {
        if (!b) throw new RuntimeException("assert failed");
    }

    /**
	 * constructor for the creation from an existing directory
	 */
	public AbstractTermMatrix(File dir) {
		this.init(terms, dir, 1, null);
//		this.initFromExisting(dir, cacheSize);
//		this.openingStatus = OpeningStatus.EXISTING;
	}

	/**
	 * Abstract method. 
	 * @param file physical location of matrix
	 * @param cacheSize a hint as of how many rows should be kept in cache, if the implementation believes in caching.
	 */
	protected abstract IntMatrixStore newIntMatrixStore(File file, int rows, int columns, int cacheSize) ;
	
	/**
	 * @param cacheSize if -1, the cache size 
	 */
	private void initFromExisting(File dir, int cacheSize) {
		this.baseDir = dir;
		if (!dir.exists())
			throw new IllegalArgumentException("expected directory [" + dir + "] to exist");

		{
			MatTextFileReader reader = new MatTextFileReader(new File(dir, TERMS_TXT));
			String[] terms = reader.getStringArray("terms");
			this.terms = terms;
			this.nterms = terms.length;
			this.terms_id = new HashMap<String, Integer>(nterms);
			for (int i = 0; i < nterms; i++) {
				this.terms_id.put(terms[i], i);
			}
		}
		
		if (cacheSize < 0) 
			throw new IllegalArgumentException("cache size must be a positive number of rows");
		
		{
			File complexitiesFile = new File(dir, COMPLEXITIES_BINMAT);
			cStore = this.newIntMatrixStore(complexitiesFile, 1, nterms, 1);
		}
		
		{
			File ccomplexitiesFile = new File(dir, COMBINED_COMPLEXITIES_BINMAT);
			ccStore = this.newIntMatrixStore(ccomplexitiesFile, nterms, nterms, cacheSize);
		}		
	}


	public int getCombinedComplexity(String term1, String term2) {
		Integer i = this.terms_id.get(term1);
		Integer j = this.terms_id.get(term2);
		if (i == null) throw new IllegalArgumentException("cannot find row labeled " + term1);
		if (j == null) throw new IllegalArgumentException("cannot find column labeled " + term2);
		return this.ccStore.get(i, j);
	}

	public int[] getCombinedComplexityRow(String term) {
		int i = this.terms_id.get(term);
		return ccStore.copyOfRow(i);
	}

	public int getTermIndex(String term) {
		return this.terms_id.get(term);
	}

	public String[] getTerms() {
		return this.terms;
	}

	public void setTerms(String[] terms) {
		assertNull(this.terms); // you may only set terms once
		
		this.terms = terms;
		
		if (terms == null) return;
		
		this.nterms = terms.length;
		this.terms_id = new HashMap<String, Integer>(nterms);
		for (int i = 0; i < nterms; i++) {
			this.terms_id.put(terms[i], i);
		}
	}

    private void assertNull(Object obj) {
        if (obj != null) throw new RuntimeException("assertion failed : expected null");
    }


    public String getTerm(int index) {
		return this.terms[index];
	}
	
	public void setCombinedComplexity(String term1, String term2, int value) {
		int i = this.terms_id.get(term1);
		int j = this.terms_id.get(term2);

		this.ccStore.put(i, j, value);
	}

	public int getComplexity(String term) {
		Integer i = this.terms_id.get(term);
		if (i == null)
			throw new IllegalArgumentException("no such term [" + term + "]");
		return this.cStore.get(0, i);
	}

	public void setComplexity(String term, int value) {
		int i = this.terms_id.get(term);
		this.cStore.put(0, i, value);
	}

	public void close() {
		if (this.exporter != null)
			this.exporter.close();
		this.cStore.close();
		this.ccStore.close();
	}

	public long getVariable(String name) {
		if (this.variables.containsKey(name))
			return ((Long)this.variables.get(name)).longValue();
		File termTextFile = new File(this.baseDir, TERMS_TXT);
		MatTextFileReader reader = new MatTextFileReader(termTextFile);
		Long longScalar = reader.getLongScalar(name);
		reader.close();
		if (longScalar == null)
			throw new RuntimeException("cannot find variable [" + name + "] in term matrix " + this.baseDir);
		this.variables.put(name, longScalar); // cache it
		return longScalar.longValue();
	}

	public void setVariable(String name, long value) {
		this.exporter.writeScalar(name, value, "");
	}

    public void setStringVariable(String name, String value) {
        this.exporter.writeStringArray(name, new String[] {value}, "");
    }

    public String getStringVariable(String name) {
        return (String) this.variables.get(name);
    }

    public File getTermMatFilename() {
		return new File(this.baseDir, TERMS_TXT);
	}
	
	public File getComplexitiesBinFilename() {
		return new File(this.baseDir, COMPLEXITIES_BINMAT);
	}
	
	public File getCombinedComplexitiesBinFilename() {
		return new File(this.baseDir, COMBINED_COMPLEXITIES_BINMAT);
	}
	
	static Logger LOG = Logger.getLogger(AbstractTermMatrix.class.getCanonicalName());
}
