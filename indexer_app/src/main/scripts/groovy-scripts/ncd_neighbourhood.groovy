import inform.dist.ngd.WeightedTerm
import inform.dist.*
import matrix.store.*

/**
 * looks into a term matrix and gets semantic neighbourhood of a term
 */
 
def run(String[] args) {
	String termMatrixLocation = args[0]
	String term = args[1]
	
	TermMatrixReadOnly tm = new TermMatrixReadOnly(new File(termMatrixLocation));
	terms = tm.terms
	
	c_term = tm.getComplexity(term)
	println "C($term) = $c_term"
	assert c_term != -1
	
	List calculatedTerms = []
	int[] ccs = tm.getCombinedComplexityRow(term)
	
	assert ccs.size() == terms.size()
	
	for (i in 0..<ccs.size()) {
		if (ccs[i] != -1) {
			trm = terms[i]
			if (term == trm) continue;
			c_term2 = tm.getComplexity(trm)
			if (c_term2 < c_term / 10) continue;
			
			cc = ccs[i];
			//dist = DistanceCalculator.getUnnormalizedDistance(c_term, c_term2, cc)
			dist = DistanceCalculator.getNormalizedDistance(c_term, c_term2, cc)
			wt = new WeightedTerm(trm, dist, i);
			calculatedTerms << wt 
		}
	}
	
	println "on a total of ${calculatedTerms.size()} calculated terms"
	calculatedTerms.sort()
	//to = Math.min(100,calculatedTerms.nrLines())
	to = calculatedTerms.size()
	calculatedTerms[0..<to].each {		
		println it	
	}
}

run(args)
