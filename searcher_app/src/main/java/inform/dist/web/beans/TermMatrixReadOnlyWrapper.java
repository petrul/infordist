package inform.dist.web.beans;

import java.io.File;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import matrix.store.TermMatrixReadOnly;

/**
 * Wrapper for {@link TermMatrixReadOnly} as stupid JSF cannot inject
 * constructor parameters
 * 
 * @author dadi
 * 
 */
public class TermMatrixReadOnlyWrapper extends TermMatrixReadOnly {
	public TermMatrixReadOnlyWrapper() {
		String dsLocation = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("app.matrix.location");
		File dsFile = new File(dsLocation);
		if (!dsFile.exists())
			throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
		LOG.info("datasource matrix is located at [" + dsLocation + "]");
		super.init(null, dsFile, 1, null);
	}
	
	static Logger LOG = Logger.getLogger(TermMatrixReadOnlyWrapper.class);
}