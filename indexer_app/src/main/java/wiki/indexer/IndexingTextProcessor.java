package wiki.indexer;

import inform.dist.Constants;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;

/**
 * "production" implementation of {@link TextProcessor}. Each wikipedia is a
 * page {@link Document} in a Lucene {@link IndexWriter}.
 * 
 * @author dadi
 * 
 */
public class IndexingTextProcessor implements TextProcessor {

	IndexWriter indexWriter;
//	Chunker		chunker;

	public IndexingTextProcessor(IndexWriter indexWriter) {
		this.indexWriter = indexWriter;
//		this.chunker = chunker;
	}

	@Override
	public void processText(StringBuilder title, StringBuilder textBody) {
		String trimmedId = title.toString().trim();
//		TextAndId[] chunks = this.chunker.chunk(new TextAndId(trimmedId, textBody.toString()));
		
//		for (TextAndId chunk : chunks) {
			String id = trimmedId;
			String txt = textBody.toString();
			
			Document doc = new Document();
			
			doc.add(new Field(Constants.FIELD_ID, id.trim(), Store.YES, Index.NO));
			doc.add(new Field(Constants.FIELD_TEXT, txt, Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS));
	
			try {
				this.indexWriter.addDocument(doc);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * aka total nr of paragraph counter
	 */
	long paragraphCounter = 0;

	@Override
	public long getParagraphCounter() {
		return this.paragraphCounter;
	}

	
	Logger LOG = Logger.getLogger(IndexingTextProcessor.class);
}
