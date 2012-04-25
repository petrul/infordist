package inform.dist.nld.cache;

import java.io.OutputStream;
import java.util.List;

/**
 * The gist of a word is the whole word context in which it appears in a corpus.
 * 
 * @author dadi
 *
 */
public interface Gist {
	
	void writeTo(OutputStream os);
	
	/**
	 * @return the number of contexts (which should be the same with the number of apparitions
	 * of the gist's term in the corpus)
	 */
	long size();
	
	/**
	 * @return an <emph>approximate</emph> of what this takes in bytes.
	 */
	long getSizeInBytes();
	
	/**
	 * @return a copy of this such that modifying the copy leave this alone
	 */
	Gist clone();
	
	/**
	 * Combine two gists..
	 */
	void combine(Gist anotherGist);

	/**
	 * break up a big gist into small ones
	 */
	List<Gist> getSubgists(int subgistSizeInBytes);

}
