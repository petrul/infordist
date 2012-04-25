package wiki.indexer.exception;

import org.xml.sax.SAXException;

/**
 * thrown in order to interrupt the sax parsing when a certain number of wikipedia pages were reached
 * @author dadi
 *
 */
public class MaxPagesReachedException extends SAXException {

	private static final long serialVersionUID = 1L;
	long nrPages;
	
	
	public MaxPagesReachedException(long nrPages) {
		this.nrPages = nrPages;
	}


	public long getNrPages() {
		return nrPages;
	}
}
