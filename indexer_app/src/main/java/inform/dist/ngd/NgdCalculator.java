package inform.dist.ngd;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;

public class NgdCalculator {

	IndexSearcher 	indexSearcher;
	Analyzer		analyzer;
	QueryParser 	qparser;
	double 			log_totalNumberOfDocuments; 
	
	static final double LN2 = Math.log(2); 
	
//	public NgdCalculator(IndexSearcher searcher, Analyzer analyzer) {
//		this.setIndexSearcher(searcher);
//		this.setAnalyzer(analyzer);
//	}
//	
//	public double getNgd(Term first, Term second) {
//      		TermQuery q1 = new TermQuery(first);
//		TermQuery q2 = new TermQuery(second);
//		BooleanQuery bQ = new BooleanQuery();
//		bQ.add(q1, Occur.MUST);
//		bQ.add(q2, Occur.MUST);
//
//		try {
//			return internalGetNgd(q1, q2, bQ);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public double getNgd(String first, String second) {
//		
//		try {
//			Query q1 = qparser.parse(first);
//			Query q2 = qparser.parse(second);
//			Query qboth = qparser.parse(
//					new StringBuilder().append(first)
//						.append(" AND ")
//						.append(second).toString());
//			
//			return internalGetNgd(q1, q2, qboth);
//			
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} catch (ParseException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private double internalGetNgd(Query q1, Query q2, Query qboth) throws IOException {
//		int hits1 = indexSearcher.search(q1, null, 1).totalHits;
//		int hits2 = indexSearcher.search(q2, null, 1).totalHits;
//		int hitsBoth = indexSearcher.search(qboth, null, 1).totalHits;
//		
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("hits for " + q1 + " = " + hits1);
//			LOG.debug("hits for " + q2 + " = " + hits2);
//			LOG.debug("hits for both = " + hitsBoth);
//		}
//		
//		return getNgdFromFreqs(hits1, hits2, hitsBoth, this.log_totalNumberOfDocuments);
//	}

	
	/**
	 * given the number of hits and the natural logarithm (base e) of the total number of documents, calculate 
	 * the NGD formula.
	 */
	static public double getNgdFromFreqs(int hits1, int hits2, int hitsBoth, double ln_totalHits) {

		double ln_hits1 = Math.log(hits1);
		double ln_hits2 = Math.log(hits2);
		double ln_hitsBoth = Math.log(hitsBoth);
		
		double max_ln_hits, min_ln_hits ;
		
		if (ln_hits1 > ln_hits2) 
			{ max_ln_hits = ln_hits1; min_ln_hits = ln_hits2;} 
		else 
			{ max_ln_hits = ln_hits2; min_ln_hits = ln_hits1;}
		
		double result = (max_ln_hits - ln_hitsBoth) 
				/ (ln_totalHits - min_ln_hits);
		
//		if (Double.isInfinite(result)) 
//			result = 1.0;
		
		return result;
	}

	
	/**
	 * retrieves the Unnormalized google distance, which is NGD without the
	 * denominator. : log max (fx, fy) - log f(xy) 
	 */
	static public double getUngdFromFreqs(int hits1, int hits2, int hitsBoth) {
		double log_hits1 = Math.log(hits1);
		double log_hits2 = Math.log(hits2);
		double log_hitsBoth = Math.log(hitsBoth);
		
		double max_log_hits = Math.max(log_hits1, log_hits2);
				
		double result = (max_log_hits - log_hitsBoth) / LN2;
		
		return result;

	}

	/**
	 * Retrieves the sheer conditional complexity, which is unsymmetrical; it is like UNGD,
	 * without the max. K(y|x) = K(x,y) - K(x). Rewritten in terms of frequencies, 
	 * this is G(x,y) - G(x) = log (hits(x)) - log (hits(x,y))
	 * 
	 * @param hits_x the number of hits of the term x, for which we want to calculate G(y|x).
	 * @param hitsBoth the number of hits for both terms, together
	 * 
	 */
	static public double getConditionalComplexityFromFreqs(int hits_x, int hitsBoth) {
		double log_hits_x = Math.log(hits_x);
		double log_hitsBoth = Math.log(hitsBoth);
		
		double result = (log_hits_x - log_hitsBoth) / LN2;
		
		return result;

	}
	
	static public double getComplexityFromFreq(int hits_x, double ln_totalHits) {
		double log_hits_x = Math.log(hits_x);
		return (ln_totalHits - log_hits_x) / LN2;
	}
	
	/*
	 * G(y|x)/G(y) = G(x,y)/G(y) - 1 = (log M - log f_xy) / (log M - log f_y) - 1 
	 */
	static public double getNormalizedConditionalComplexityFromFreqs(int hits_y, int hitsBoth, double ln_totalHits) {
		double log_hits_y = Math.log(hits_y);
		double log_hitsBoth = Math.log(hitsBoth);
		
		double result = (ln_totalHits - log_hitsBoth) / (ln_totalHits - log_hits_y) - 1.0;
		
		return result;

	}
	
	
//	/**
//	 * alias for {@link #getNgdMatrix(String[])}
//	 */
//	public double[][] getNgdMatrix(List<Term> words, double[][] matrix) {
//		return this.getNgdMatrix((Term[]) words.toArray(new Term[words.size()]), matrix);
//	}
	
//	/**
//	 * given a word list, returns a symmetric matrix with distances
//	 */
//	public double[][] getNgdMatrix(Term[] terms, double[][] result) {
//		int nterms = terms.length;
//		//double[][] result = new double[nterms][nterms];
//		
//		int counter = 0;
////		int counterSkipped = 0;
//		
//		for (int i = 0; i < nterms; i++) {
//			result[i][i] = 0.0;
//			Term term_i = terms[i];
//			for (int j = i + 1; j < nterms; j++) {
//				Term term_j = terms[j];
//				
//				if (result[i][j] != 0.0)
//					continue;
//				
//				double ngd = this.getNgd(term_i, term_j);
//				
//				// the following two are atomic
//				synchronized(result) {
//					result[i][j] = ngd;
//					result[j][i] = ngd;
//				}
//			}
//			
//			if (counter % 1 == 0) {
//				LOG.info("" + counter + " terms ");
//			}
//			counter++;
//		}
//		
//		return result;
//	}

//	@Deprecated
//	public SemanticNeighborhood getSemanticNeighborhood(int maxTerms) {
//		try {
//			StopWatch sw = new StopWatch();sw.start();
//			LOG.info("getting list of terms in decreasing frequency order...");
//			List<TermAndFreq> terms = new IndexUtil(this.indexSearcher.getIndexReader()).getTermsOrderedByFreqDesc();
//			LOG.info("done, " + terms.size() + " terms retrieved, took" + sw.toString() + ". will calculate pairwise distances now...");
//			
//			sw.reset(); sw.start();
//			
//			SemanticNeighborhood sn = new SemanticNeighborhood();
//			
//			int counter = 0;
//			for (TermAndFreq tf : terms) {
//				
//				TreeSet<WeightedTerm> neighbours = new TreeSet<WeightedTerm>();
//				Term t = new Term("text", tf.getTerm());
//				for (Term existingTerm : sn.termSet()) {
//					if (t == existingTerm) continue;
//					double ngd = this.getNgd(t, existingTerm);
//					sn.addNeighbour(existingTerm, new WeightedTerm(t, ngd));
//					neighbours.add(new WeightedTerm(existingTerm, ngd));
//				}
//				sn.addTerm(t, neighbours);
//				counter++;
//				
//				if (counter % 100 == 0)
//					LOG.info("calculated NGD distances to " + counter + " terms, current term is [" + t.text() + "], freq=" + this.indexSearcher.docFreq(t));
//				
//				if (counter > maxTerms) {
//					LOG.info("reached limit of " + maxTerms + ", will stop.");
//					break;
//				}
//
//				LOG.info("done, took " + sw);
//			}
//			return sn;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	@Deprecated
//	public SemanticNeighborhood getSemanticNeighborhood() {
//		return this.getSemanticNeighborhood(Integer.MAX_VALUE);
//	}
	
	// accessors 
	
//	public IndexSearcher getIndexSearcher() {
//		return indexSearcher;
//	}
//
//	public void setIndexSearcher(IndexSearcher indexSearcher) {
//		this.indexSearcher = indexSearcher;
//		int totalNumberOfDocuments = indexSearcher.getIndexReader().maxDoc();
//		LOG.info("total number of documents :" + totalNumberOfDocuments);
//		this.log_totalNumberOfDocuments = Math.log(totalNumberOfDocuments);
//	}
//
//	public Analyzer getAnalyzer() {
//		return analyzer;
//	}
//
//	public void setAnalyzer(Analyzer analyzer) {
//		this.analyzer = analyzer;
//		this.qparser = new QueryParser("text", this.analyzer);
//	}


	Logger LOG = Logger.getLogger(NgdCalculator.class);
}
