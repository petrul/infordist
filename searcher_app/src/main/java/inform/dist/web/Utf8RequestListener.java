package inform.dist.web;

import java.io.UnsupportedEncodingException;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;

public class Utf8RequestListener implements PhaseListener {
	 
	private static final long serialVersionUID = 1L;

	public void beforePhase(PhaseEvent e) {
        if (e.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
            ServletRequest request = (ServletRequest) e.getFacesContext().getExternalContext().getRequest();
            try {
                request.setCharacterEncoding("UTF-8");
                LOG.info("term in jsf listener : " + request.getParameter("term"));
            } catch (UnsupportedEncodingException uee) {
            	LOG.error(uee, uee);
            }
        }
    }
 
    public void afterPhase(PhaseEvent e) {
    }
 
    public PhaseId getPhaseId() {
        return javax.faces.event.PhaseId.ANY_PHASE;
    }
    
    Logger LOG = Logger.getLogger(Utf8RequestListener.class);
}


