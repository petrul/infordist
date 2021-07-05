package inform.dist.web;

import inform.dist.ngd.DistanceCalculatorFromFreqMatrix;
import inform.dist.web.beans.TermService;
import matrix.store.TermMatrixReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

// springy app context
public class ApplicationContext {

    protected static ApplicationContext INSTANCE = null;

    TermMatrixReadOnly termMatrix;
    TermService termService;
    DistanceCalculatorFromFreqMatrix distCalculator;
    NgdNeighboursService ngdNeighboursService;

    static public ApplicationContext getInstance(ServletContext servletContext) {
        synchronized (ApplicationContext.class) {
            if (INSTANCE == null)
                INSTANCE = new ApplicationContext(servletContext);
            return INSTANCE;
        }
    }

    public static String searchForMatrixLocation(ServletContext servletContext) {
        String matrixLocation = System.getenv("INFORDIST_MATRIX_LOCATION");
        if (matrixLocation == null) {
            matrixLocation = System.getProperty("infordist.matrix.location");
            if (matrixLocation == null) {
                matrixLocation = servletContext.getInitParameter("infordist.matrix.location");
                if (matrixLocation == null)
                    throw new IllegalArgumentException("define the INFORDIST_MATRIX_LOCATION env variable or run java -Dinfordist.matrix.location=/my/term/matrix or define infordist.matrix.location init param in web.xml");
            }
        }
        return matrixLocation;
    }

    private ApplicationContext(ServletContext servletContext) {
        final String matrixLocation = searchForMatrixLocation(servletContext);
        File dsFile = new File(matrixLocation);
        if (!dsFile.exists())
            throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
        LOG.info("datasource matrix is located at [" + matrixLocation + "]");

        this.termMatrix = new TermMatrixReadOnly(new File(matrixLocation));
        this.termService = new TermService(this.termMatrix);

        this.distCalculator = new DistanceCalculatorFromFreqMatrix(this.termMatrix);
        this.ngdNeighboursService = new NgdNeighboursService(this.termService);

    }

    public TermMatrixReadOnly getTermMatrix() {
        return termMatrix;
    }

    public TermService getTermService() {
        return termService;
    }

    public DistanceCalculatorFromFreqMatrix getDistCalculator() {
        return distCalculator;
    }

    public NgdNeighboursService getNgdNeighboursService() {
        return ngdNeighboursService;
    }

    static Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);
}
