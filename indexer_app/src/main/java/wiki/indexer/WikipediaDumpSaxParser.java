package wiki.indexer;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import wiki.indexer.exception.MaxPagesReachedException;

/**
 * Sax parser for the 18GB wikipedia XML file; for each &lt;page>, it isolates
 * the id and the
 * @author dadi
 *
 */
public class WikipediaDumpSaxParser implements ContentHandler {

	enum ProcessingWhat { TITLE, TEXT, NOTHING }
	
	TextProcessor pureTextProcessor;

	long maxPages = Integer.MAX_VALUE;
	
	StringBuilder crtMainText;
	StringBuilder crtTitle;

	ProcessingWhat processingWhat; 
	
	/* statistics */
	
	/**
	 * how many pages were processed
	 */
	long pageCounter = 0;
	long textWindowCounter = 0;

	
	public WikipediaDumpSaxParser(TextProcessor processor) {
		this.setPureTextProcessor(processor);
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		if ("page".equals(localName)) {
			this.crtMainText = new StringBuilder();
			this.crtTitle = new StringBuilder();
			this.pageCounter++;
			if (this.pageCounter % 10000 == 0)
				LOG.info("indexed " + this.pageCounter + " pages");
			if (this.pageCounter > this.maxPages)
				throw new MaxPagesReachedException(this.maxPages);
		} else
			
		if ("title".equals(localName)) {
			this.processingWhat = ProcessingWhat.TITLE;
		} else
			
		if ("text".equals(localName)) {
			this.processingWhat = ProcessingWhat.TEXT;
		} else
		
		this.processingWhat = ProcessingWhat.NOTHING;
	}


	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (this.processingWhat == ProcessingWhat.TEXT)
			this.crtMainText.append(ch, start, length);
		else
		if (this.processingWhat == ProcessingWhat.TITLE)
			this.crtTitle.append(ch, start, length);
		else
		;
	}


	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if ("page".equals(localName)) {
			this.pureTextProcessor.processText(this.crtTitle, this.crtMainText);
			this.textWindowCounter = this.pureTextProcessor.getParagraphCounter();
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		LOG.info("done");
	}

	// accessors
	
	public long getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(long maxPages) {
		this.maxPages = maxPages;
	}

	public void setPureTextProcessor(TextProcessor pureTextProcessor) {
		this.pureTextProcessor = pureTextProcessor;
	}
	
	public long getPageCounter() {
		return pageCounter;
	}

	public long getTextWindowCounter() {
		return textWindowCounter;
	}

	Logger LOG = Logger.getLogger(WikipediaDumpSaxParser.class);
}
