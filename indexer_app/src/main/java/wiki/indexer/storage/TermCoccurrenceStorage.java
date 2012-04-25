package wiki.indexer.storage;

/**
 * stores terms, their absolute frequency and their relative co-occurrence
 * frequency
 * 
 * @author dadi
 *
 */
public interface TermCoccurrenceStorage {

	void increaseTermFreq(String crtTerm);

	void markCooccurrence(String term1, String term2);

}
