package inform.dist.web.beans;

import edu.emory.mathcs.backport.java.util.Collections;
import inform.dist.ngd.NgdCalculator;
import matrix.store.TermMatrixReadOnly;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import java.util.*;

public class TermService {
	
	//JdbcTemplate 			jdbc ;
	int 					totalDocs ;
	Map<String, Integer> 	termsAndFreqs ;
	TermMatrixReadOnly 		datasource ;
	
	final static public double LN2 = Math.log(2.0);
	
	static LRUMap cache = new LRUMap(4 * 10);
	
	public Map<String, Double> gcomplexity = new HashMap<String, Double>() {
		private static final long serialVersionUID = 1L;
		@Override
		public Double get(Object key) {
			return getGComplexity((String)key);
		}
	};
	
	public Map<String, Double> getGcomplexity() {
		return gcomplexity;
	}
	
	public Map<String, Integer> termAbsolutFreq = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		@Override
		public Integer get(Object key) {
			return getTermAbsFreq((String)key);
		}
	};
	
	public Map<String, Integer> getTermAbsolutFreq() {
		return termAbsolutFreq;
	}
	

	/**
	 * how many bits needed to represent given term log(N/n)
	 */
	public Double getGComplexity(String term) {
		if (term == null)
			return null;
		Integer termAbsFreq = this.getTermAbsFreq(term);
		if (termAbsFreq == null)
			return null;
		return Math.log(((double)this.getTotalDocs()) / termAbsFreq) / LN2; 
	}

	public Integer getTermAbsFreq(String term) {
		try {
			if (term == null)
				return null;
			//return this.jdbc.queryForInt("select absfreq from terms where term collate 'utf8_bin' = ?", new Object[] {term});
			return this.termsAndFreqs.get(term);
		} catch (DataAccessException e) {
			LOG.error(e,e);
			return -1;
		}
	}
	
//	@SuppressWarnings("unchecked")
//	public void setDatasource(JndiDatasource ds) {
//		this.jdbc = new JdbcTemplate(ds.getDatasource());
//		int nrterms = this.jdbc.queryForInt("select count(*) from terms");
//		this.termsAndFreqs = new HashMap<String, Integer>(nrterms);
//		List<Map<String,Object>> resultset = this.jdbc.queryForList("select term, absfreq from terms");
//		for (Map<String,Object> record: resultset) {
//			String term = (String) record.get("term");
//			Integer freq = (Integer) record.get("absfreq");
//			this.termsAndFreqs.put(term, freq);
//		}
//		this.totalDocs = this.jdbc.queryForInt("select value from vars where name = 'total_docs'");
//	}


	public TermService() {}

	public TermService(TermMatrixReadOnly datasource) {
		this.setDatasource(datasource);
	}

	public void setDatasource(TermMatrixReadOnly ds) {
		this.datasource = ds;
		int nrterms = ds.getTerms().length;// this.jdbc.queryForInt("select count(*) from terms");
		this.termsAndFreqs = new HashMap<String, Integer>(nrterms);
		for (String term : ds.getTerms()) {
			int freq = this.datasource.getComplexity(term);
			this.termsAndFreqs.put(term, freq);
		}
		this.totalDocs = (int) this.datasource.getVariable("total_docs");
	}
	
	public int getTotalDocs() {
		return totalDocs;
	}

	
	/**
	 * gets the combined complexities of the term given as parameter with all other
	 * terms.
	 */
	public Map<String, Integer> getCombinedComplexities(String term) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (String t : this.datasource.getTerms()) {
			int cc = this.datasource.getCombinedComplexity(term, t);
			result.put(t, cc);
		}
		
		return result;
	}
	
	
	public List<Map<String, Object>> getNgdNeighbours(String term) {
		
		Map<String, Integer> cc_list = this.getCombinedComplexities(term);
		ArrayList<Map<String, Object>> result = new ArrayList<>();
		Integer thisTermsAbsFreq = this.getTermAbsFreq(term);
		
		for (String t : cc_list.keySet()) {
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put("text", t);
			Integer cc = cc_list.get(t);
			if (cc == 0) continue;
			
			Integer thatTermsAbsFreq = this.getTermAbsFreq(t);
			Double ngd = NgdCalculator.getNgdFromFreqs(thisTermsAbsFreq, thatTermsAbsFreq, cc, Math.log(totalDocs));
			map.put("dist", ngd);
			result.add(map);
		}
		
		sortArray(result);
		
		return result;
	}
	
	static public void sortArray(ArrayList<Map<String, Object>> result) {
		Collections.sort(result, new Comparator<Map<String,Object>>()  {

			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Double i1 = (Double) o1.get("dist");
				Double i2 = (Double) o2.get("dist");
				return i1.compareTo(i2);
			}
		});		
	}
	
//	public List<Map<String, Object>> getNeighbours(String term, String kind) {
//		return null;
		
//		if (term == null)
//			return null;
//		
//		if (cache.containsKey(kind + "+" + term)) {
//			return (List<Map<String, Object>>) cache.get(kind + "+" + term);
//		}
//		
//		int termId; 
//		try {
//			termId = jdbc.queryForInt("select id from terms where term collate 'utf8_bin' = ?", new Object[] {term});
//			
//			String getNeighboursQuery = "select \n" + 
//				"  terms.id as id, " + 
//				"  terms.term as text, " + 
//				"  $MATRIX.value as dist " + 
//				"from $MATRIX, terms " + 
//				"where terms.id = $MATRIX.term2 and $MATRIX.term1 = ? order by dist asc"; 
//
//			List<Map<String, Object>> rowsNgd = jdbc.queryForList(getNeighboursQuery.replace("$MATRIX", kind +"_matrix" ), new Object[] {termId});
//			cache.put(kind + "+" + term, rowsNgd);
//			return rowsNgd;
//
//		} catch (EmptyResultDataAccessException e) {
//			FacesContext.getCurrentInstance().addMessage("don't know what to put here", new FacesMessage("dont't know term [" + term + "]"));
//			LOG.warn("for term " + term, e);
//			return null;
//		} catch (IncorrectResultSizeDataAccessException e) {
//			LOG.warn("for term " + term, e);
//			FacesContext.getCurrentInstance().addMessage("don't know what to put here", new FacesMessage("dont't know term [" + term + "]"));
//			return null;
//		} catch (DataAccessException e) {
//			LOG.warn("for term " + term, e);
//			FacesContext.getCurrentInstance().addMessage("don't know what to put here", new FacesMessage("dont't know term [" + term + "]"));
//			return null;
//		} 

//	}
	
	static Logger LOG = Logger.getLogger(TermService.class);

}
