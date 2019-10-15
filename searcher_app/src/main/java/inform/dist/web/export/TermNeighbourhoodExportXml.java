package inform.dist.web.export;

import inform.dist.web.beans.TermService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import matrix.store.TermMatrixReadOnly;

public class TermNeighbourhoodExportXml extends HttpServlet {
	private static final long serialVersionUID = 1L;

	TermMatrixReadOnly matrix = null;
	TermService termService = null;
	
	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init(cfg);
		String dsLocation = cfg.getServletContext().getInitParameter("app.matrix.location");
		File dsFile = new File(dsLocation);
		if (!dsFile.exists())
			throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
		LOG.info("datasource matrix is located at [" + dsLocation + "]");
		this.matrix = new TermMatrixReadOnly(dsFile, 1);
		this.termService = new TermService();
		this.termService.setDatasource(this.matrix);
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String term = req.getParameter("term");
		if (term == null || "".equals(term.trim())) {
			resp.getWriter().write("<error>no term</error>");
			return;
		}
		List<Map<String,Object>> neighbours = this.termService.getNgdNeighbours(term);
		XStream xstream = new XStream();
		xstream.toXML(neighbours, resp.getWriter());
	}
	
	static Logger LOG = Logger.getLogger(TermNeighbourhoodExportXml.class);
}
