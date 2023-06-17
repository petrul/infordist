package inform.dist.web.beans;

import lombok.extern.log4j.Log4j2;
import matrix.store.TermMatrixReadOnly;

import javax.faces.context.FacesContext;
import java.io.File;

/**
 * Wrapper for {@link TermMatrixReadOnly} as stupid JSF cannot inject
 * constructor parameters
 * 
 * @author dadi
 * 
 */
@Log4j2
public class TermMatrixReadOnlyWrapper extends TermMatrixReadOnly {
	public TermMatrixReadOnlyWrapper() {
		super(new File(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("infordist.matrix.location")), 2);
		String dsLocation = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("infordist.matrix.location");
		File dsFile = new File(dsLocation);
		if (!dsFile.exists())
			throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
		log.info("datasource matrix is located at [" + dsLocation + "]");
	}

}
