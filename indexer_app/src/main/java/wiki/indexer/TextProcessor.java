package wiki.indexer;

/**
 * When the Sax Parser {@link WikipediaDumpSaxParser} finds a chunk of text, it
 * gives it to a {@link TextProcessor}. It could have done the processing
 * itself, but the abstraction of the processor in a separate component allows
 * for testing of the Lucene {@link WikipediaSnowballAnalyzer} of a pure chunk
 * of text.
 * 
 * @author dadi
 */
public interface TextProcessor {

	void processText(StringBuilder id, StringBuilder textBody);

	/**
	 * @return the total number of paragraphs (text windows) that were processed
	 *         by this processor until now.
	 */
	public long getParagraphCounter();
}
