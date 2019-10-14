package wiki.indexer;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import wiki.indexer.storage.TermCoccurrenceStorage;

public class FrequencyStoringTextProcessor implements TextProcessor {

	Analyzer				analyzer;
	
	TermCoccurrenceStorage 	storage;
	
	char[] tmpParagraphBuffer = new char[10 * 1000]; // paragraphs maximum nrLines
	
	/**
	 * aka total nr of paragraph counter 
	 */
	long 					paragraphCounter = 0; 

	
	public FrequencyStoringTextProcessor(Analyzer analyzer, TermCoccurrenceStorage storage) {
		this.analyzer = analyzer;
		this.storage = storage;
	}

	@Override
	public void processText(StringBuilder title, StringBuilder textBody) {
		
		int crtIndex = 0;
		int paraCounter = 0;
		
		while (crtIndex != -1) {

			int oldIndex = crtIndex;
			crtIndex = textBody.indexOf("\n\n", crtIndex);

			if (crtIndex != -1) crtIndex += 2; // include the double carriage-return
			
			paraCounter ++;
			this.paragraphCounter ++;
			int endIndex = crtIndex == -1 ? textBody.length() : crtIndex;
			
			// huge paragraphs must be reduced to acceptable dimensions
			if (endIndex - oldIndex > this.tmpParagraphBuffer.length) {
				endIndex = oldIndex + this.tmpParagraphBuffer.length;
				crtIndex = endIndex;
			}
			int paraLength = endIndex - oldIndex;

			textBody.getChars(oldIndex, endIndex, tmpParagraphBuffer, 0);
			TokenStream tokenStream = this.analyzer.tokenStream("text", new CharArrayReader(tmpParagraphBuffer, 0, paraLength));
			Token tk = new Token();
			Set<String> termsInThisWindow = new HashSet<String>();

			try {

				while (tokenStream.next(tk) != null) {
					String crtTerm = tk.term();
					this.storage.increaseTermFreq(crtTerm);
					termsInThisWindow.add(crtTerm);
				}

				ArrayList<String> termsArr = new ArrayList<String>(termsInThisWindow);
				for (int i = 0; i < termsArr.size(); i++) {
					for (int j = i + 1; j < termsArr.size(); j++) {
						this.storage.markCooccurrence(termsArr.get(i), termsArr.get(j));
					}
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	
	public TermCoccurrenceStorage getStorage() {
		return storage;
	}

	public void setStorage(TermCoccurrenceStorage storage) {
		this.storage = storage;
	}

	public long getParagraphCounter() {
		return paragraphCounter;
	}
	
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

}
