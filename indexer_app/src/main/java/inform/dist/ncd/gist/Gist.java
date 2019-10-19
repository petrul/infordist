package inform.dist.ncd.gist;

import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.gist.combining.GistCombiningPolicy;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The string of a word is the whole word context in which it appears in a corpus.
 *
 * Physically it is a text file with lines of approx. 5 words.
 * 
 * @author dadi
 *
 */
public interface Gist extends Iterable<String> {


	
	/**
	 * @return the number of contexts (which should be the same with the number of apparitions
	 * of the string's term in the corpus) -- for example the number of lines. NB. this is not {@link #getSizeInBytes()}!
	 */
	int nrLines();
	
	/**
	 * @return an <emph>approximate</emph> of what this takes in bytes.
	 * ?Why approximate?
	 */
	long getSizeInBytes();
	
	/**
	 * Combine two gists.
	 */
	void combine(Gist anotherGist, GistCombiningPolicy.Policy policy, OutputStream outputStream);

	long computeComplexity(Compressor compressor);

	/**
	 * use this to read in a stream from this string
	 */
	InputStream openStreamForReading();

//	/**
//	 * use this method to write in a stream to this string
//	 */
//	OutputStream openStreamForWriting();

	void writeTo(OutputStream os);

}
