package inform.dist.web.beans;

import inform.dist.Constants;
import inform.dist.ngd.DistanceCalculatorFromFreqMatrix;
import inform.dist.ngd.NgdCalculator;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import lombok.extern.log4j.Log4j2;
import matrix.store.TermMatrixReadOnly;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import wiki.indexer.WikipediaSnowballAnalyzer;

/**
 * JSF Bean behind neighbours.jspx 
 * 
 * @author dadi
 *
 */
@Log4j2
public class TermBean  {
	
	String term;
	
	TermService termService;
	
	Preferences preferences;
	
	TermMatrixReadOnly datasource;
	DistanceCalculatorFromFreqMatrix dCalc;

	public TermBean() {
		log.info("constructor of TermBean");
	}

	public List<Map<String, Object>> getNgdNeighbours() {
		return this.termService.getNgdNeighbours(term);
	}
	
	
	public List<Map<String, Object>> getUngdNeighbousrs() {

		Map<String, Integer> cc_list = termService.getCombinedComplexities(this.term);
		
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		Integer thisTermsAbsFreq = termService.getTermAbsFreq(this.term);
		
		for (String t : cc_list.keySet()) {
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put("text", t);
			Integer cc = cc_list.get(t);
			if (cc == 0) continue;
			
			Integer thatTermsAbsFreq = termService.getTermAbsFreq(t);
			Double ngd = NgdCalculator.getUngdFromFreqs(thisTermsAbsFreq, thatTermsAbsFreq, cc);
			map.put("dist", ngd);
			result.add(map);
		}
		
		TermService.sortArray(result);
		
		return result;

		
	}
	
	public List<Map<String, Object>> getNccFromHereNeighbours() {
		Map<String, Integer> cc_list = termService.getCombinedComplexities(this.term);
		
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		Integer thisTermsAbsFreq = termService.getTermAbsFreq(this.term);
		
		for (String t : cc_list.keySet()) {
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put("text", t);
			Integer cc = cc_list.get(t);
			if (cc == 0) continue;
			
			Integer thatTermsAbsFreq = termService.getTermAbsFreq(t);
			Double ngd = NgdCalculator.getConditionalComplexityFromFreqs(thatTermsAbsFreq, cc);
			map.put("dist", ngd);
			result.add(map);
		}
		TermService.sortArray(result);
		return result; 
	}
	
	public List<Map<String, Object>> getNccToHereNeighbours() {
		Map<String, Integer> cc_list = termService.getCombinedComplexities(this.term);
		
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		Integer thisTermsAbsFreq = termService.getTermAbsFreq(this.term);
		
		for (String t : cc_list.keySet()) {
			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put("text", t);
			Integer cc = cc_list.get(t);
			if (cc == 0) continue;
			
			Integer thatTermsAbsFreq = termService.getTermAbsFreq(t);
			Double ngd = NgdCalculator.getConditionalComplexityFromFreqs(thisTermsAbsFreq, cc);
			map.put("dist", ngd);
			result.add(map);
		}
		
		TermService.sortArray(result);
		
		return result; 
 
	}

	public void setDatasource(TermMatrixReadOnly datasource) {
		this.datasource = datasource;
		this.dCalc = new DistanceCalculatorFromFreqMatrix(datasource);
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		log.info("setTerm:" + term);
		byte[] bytes = term.getBytes(Charset.forName("latin1"));
		String utf8term = new String(bytes, Charset.forName("utf-8"));
		WikipediaSnowballAnalyzer analyzer = new WikipediaSnowballAnalyzer("English");
		Token tk = new Token();
		TokenStream stream = analyzer.tokenStream(Constants.FIELD_TEXT, new StringReader(utf8term));
		try {
			if (stream.next(tk) == null) {
				FacesContext.getCurrentInstance().addMessage("problem", new FacesMessage("problem, man"));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.term = tk.term();
	}
	
	public void setTermService(TermService termService) {
		this.termService = termService;
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
	
}
