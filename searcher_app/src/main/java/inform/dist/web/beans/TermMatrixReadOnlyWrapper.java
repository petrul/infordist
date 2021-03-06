package inform.dist.web.beans;

import matrix.store.TermMatrixReadOnly;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import java.io.File;

/**
 * Wrapper for {@link TermMatrixReadOnly} as stupid JSF cannot inject
 * constructor parameters
 * 
 * @author dadi
 * 
 */
public class TermMatrixReadOnlyWrapper extends TermMatrixReadOnly {
	public TermMatrixReadOnlyWrapper() {
		super(new File(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("infordist.matrix.location")), 2);
		String dsLocation = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("infordist.matrix.location");
		File dsFile = new File(dsLocation);
		if (!dsFile.exists())
			throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
		LOG.info("datasource matrix is located at [" + dsLocation + "]");
	}
	
	static Logger LOG = Logger.getLogger(TermMatrixReadOnlyWrapper.class);
}
