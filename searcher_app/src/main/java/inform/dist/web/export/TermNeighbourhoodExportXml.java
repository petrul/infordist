package inform.dist.web.export;

import com.thoughtworks.xstream.XStream;
import inform.dist.web.beans.TermService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import matrix.store.TermMatrixReadOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("possibly-obsolete")
public class TermNeighbourhoodExportXml  {
	private static final long serialVersionUID = 1L;

	@Autowired
	TermMatrixReadOnly matrix;

	@Autowired
	TermService termService;

//	public void init(ServletConfig cfg) throws ServletException {
//		super.init(cfg);
//		String dsLocation = cfg.getServletContext().getInitParameter("infordist.matrix.location");
//		File dsFile = new File(dsLocation);
//		if (!dsFile.exists())
//			throw new IllegalArgumentException("no matrix datasource at location [" + dsFile +"]");
//		LOG.info("datasource matrix is located at [" + dsLocation + "]");
//		this.matrix = new TermMatrixReadOnly(dsFile, 1);
//		this.termService = new TermService();
//		this.termService.setDatasource(this.matrix);
//	}
	
	@GetMapping("?")
	public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String term = req.getParameter("term");
		if (term == null || "".equals(term.trim())) {
			resp.getWriter().write("<error>no term</error>");
			return;
		}
		List<Map<String,Object>> neighbours = this.termService.getNgdNeighbours(term);
		XStream xstream = new XStream();
		xstream.toXML(neighbours, resp.getWriter());
	}
}
