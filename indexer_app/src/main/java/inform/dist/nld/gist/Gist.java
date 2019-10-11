package inform.dist.nld.gist;

import inform.dist.nld.gist.combining.GistCombiningPolicy;

import java.io.OutputStream;
import java.util.List;

/**
 * The gist of a word is the whole word context in which it appears in a corpus.
 *
 * Physically it is a text file with lines of approx. 5 words.
 * 
 * @author dadi
 *
 */
public interface Gist {

//	/**
//	 * used by @Gist#combine
//	 *
//	 * INTERLACE_EVEN is probably the way to go.
//	 */
//	enum CombiningPolicy {
//
//		// two gists are concatenated. for big gists, compression together might not work appropriately since most
//		// compression algorithms work on blocks so block1 might be gist1 and block2 might be gist2.
//		CONCATENATE {
//			@Override
//			void execute() {
//			}
//		},
//
//		// one line from a gist, another from the other one. for gists of different sizes,
//		// when the smaller gist ends, the rest of lines of
//		// the larger gist continue unmixed
//		INTERLACE_TOP {
//			@Override
//			void execute() {
//			}
//		},
//
//		// interlaced but evenly. if we have a gist G1 of 200 lines and G2 of 100 lines,
//		// after two lines of G1 will be interlaced one line of G2
//		INTERLACE_EVEN {
//			@Override
//			void execute() {
//			}
//		};
//
//
//		abstract void execute();
//	}

	void writeTo(OutputStream os);
	
	/**
	 * @return the number of contexts (which should be the same with the number of apparitions
	 * of the gist's term in the corpus) -- for example the number of lines. NB. this is not {@link #getSizeInBytes()}!
	 */
	int size();
	
	/**
	 * @return an <emph>approximate</emph> of what this takes in bytes.
	 * ?Why approximate?
	 */
	long getSizeInBytes();
	
//	/**
//	 * @return a copy of this such that modifying the copy leaves this alone
//	 */
//	Gist cloneGist();
	
	/**
	 * Combine two gists.
	 */
	Gist combine(Gist anotherGist, GistCombiningPolicy.Policy policy);


	/**
	 * break up a big gist into small ones
	 */
	List<Gist> getSubgists(int subgistSizeInBytes);

}
