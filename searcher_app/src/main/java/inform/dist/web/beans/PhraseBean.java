package inform.dist.web.beans;

import inform.dist.Constants;
import inform.dist.ngd.WeightedTerm;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import matrix.store.TermMatrixReadOnly;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import wiki.indexer.WikipediaSnowballAnalyzer;

public class PhraseBean {
	String phrase;
	List<String> tokens = new ArrayList<String>();
//	JdbcTemplate jdbc;
	TermMatrixReadOnly datasource;
	TermService	termService;
	
	static LRUMap cache = new LRUMap(10);
	
	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		// this comes from the bugged param['phrase']
		if (phrase == null)
			phrase = "";
		this.phrase = new UtilServiceBean().latin2Utf8(phrase);
		WikipediaSnowballAnalyzer analyzer = new WikipediaSnowballAnalyzer("English");
		
		Token tk = new Token();
		TokenStream stream = analyzer.tokenStream(Constants.FIELD_TEXT, new StringReader(this.phrase));
		try {
			while (stream.next(tk) != null) {
				tokens.add(tk.term());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	@SuppressWarnings("unchecked")
	public List<WeightedTerm> getPhraseNeighbours() {
		if (cache.containsKey(this.phrase)) {
			return (List<WeightedTerm>) cache.get(this.phrase);
		}
		LOG.info(super.toString() + " calculating sense of phrase " + this.phrase);
		Set<String> allNeighbours = new HashSet<String>(1000);
		
		Map<String, List<Map<String, Object>>> tokensAndTheirNeighbours = new HashMap<String, List<Map<String,Object>>>();
		for (String token : tokens) {
			List<Map<String,Object>> neighbours = this.termService.getNgdNeighbours(token);
			try { neighbours = neighbours.subList(0, 1000); } catch (IndexOutOfBoundsException e) {}
			if (neighbours == null || neighbours.size() == 0) {
				LOG.warn("no neighbours for [" + token + "]");
				FacesContext.getCurrentInstance().addMessage("hello", 
						new FacesMessage("big big problem"));
				continue;
			}
			tokensAndTheirNeighbours.put(token, neighbours);
			for (Map<String,Object> elem : neighbours) {
				allNeighbours.add((String) elem.get("text"));
			}
			LOG.info("got neighbours for [" + token + "]");
		}
		
		Set<WeightedTerm> resultSet = new TreeSet<WeightedTerm>();
		for (String n : allNeighbours) {
			List<WeightedTerm> apparitions = new ArrayList<WeightedTerm>();
			for (String token : tokens) {
				List<Map<String, Object>> tokensNeighbours = tokensAndTheirNeighbours.get(token);
				if (tokensNeighbours == null) { continue; } // don't have this term in the database
				for (Map<String, Object> record : tokensNeighbours) {
					String name = (String) record.get("text");
					if (!name.equals(n))
						continue;
					double dist = (Double) record.get("dist");
					WeightedTerm wt = new WeightedTerm(name, dist, -1);
					apparitions.add(wt);
				}
			}
			int nrApparitions = apparitions.size();
			if (nrApparitions > 1) {
				double calculatedWeight = 0.0;
				StringBuilder logStuff = new StringBuilder();
				for (WeightedTerm wt : apparitions) {
					calculatedWeight += 1.0 / wt.getWeight();
					logStuff.append(wt.getWeight()).append(",");
				}
				calculatedWeight = 1 / calculatedWeight;
				// favour informative (complex) terms
//				calculatedWeight = calculatedWeight / this.termService.getGComplexity(n);
				WeightedTerm wt = new WeightedTerm(n, calculatedWeight, -1);
				resultSet.add(wt);
//				LOG.info("neighbour [" + wt + "] appears " + nrApparitions + " times : " + logStuff.toString());
			} else {
				WeightedTerm wt = apparitions.iterator().next();
				wt.setWeight(1.5 * wt.getWeight()); // penalization for only occurring once
				if (wt.getWeight() <= 1.0) // don't add it if it gets too far
					//resultSet.add(wt)
					;
			}
		}
		List<WeightedTerm> result = new ArrayList<WeightedTerm>(resultSet);
		cache.put(this.phrase, result);
		return result;
	}
	
	public List<String> getTokens() {
		return tokens;
	}
	
//	public void setDatasource(JndiDatasource ds) {
//		this.jdbc = new JdbcTemplate(ds.getDatasource());
//	}

	public void setDatasource(TermMatrixReadOnly ds) {
		this.datasource = ds;
	}
	
	public void setTermService(TermService termService) {
		this.termService = termService;
	}
	
	static Logger LOG = Logger.getLogger(PhraseBean.class);
}
