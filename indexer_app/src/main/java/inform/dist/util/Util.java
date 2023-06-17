package inform.dist.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wiki.indexer.TermAndFreq;

public class Util {
	
	/**
	 * @terms a list of terms
	 * @return a map where you can use a term's text string in order to retrieve its index in the list
	 * yes it's not java camelcase but it looks so much better.
	 */
	static public Map<String, Integer> get_term_id_map(List<TermAndFreq> terms, int limit) {
		HashMap<String, Integer> map = new HashMap<String, Integer>(limit);
		for (int i = 0; i < limit; i++) {
			String term = terms.get(i).getTerm();
			map.put(term, i);
		}
		return map;
	}
	
	static public String[] get_term_arr(List<TermAndFreq> terms, int limit) {
		int actualLimit = Math.min(limit, terms.size());
		
		String[] res = new String[actualLimit];
		for (int i = 0; i < actualLimit; i++) {
			res[i] = terms.get(i).getTerm();
		}
		return res;
	}
	
//	static public BidiMap termList2BidiMap(List<TermAndFreq> terms, int limit) {
//		TreeBidiMap idTermMap = new TreeBidiMap();
//		for (int i = 0; i < limit; i++) {
//			String term = terms.get(i).getTerm();
//			idTermMap.put(i, term);
//		}
//		return idTermMap;
//	}

	public static void azzert(boolean condition, String message) {
		if (! condition)
			throw new RuntimeException(message);
	}
}
