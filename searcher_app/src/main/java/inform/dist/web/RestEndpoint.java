package inform.dist.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RestEndpoint extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String term = req.getParameter("term");

        ApplicationContext applicationContext = ApplicationContext.getInstance();
        final NgdNeighboursService ngdNeighboursService = applicationContext.getNgdNeighboursService();
        final List<Map<String, Object>> ngdNeighbours = ngdNeighboursService.getNgdNeighbours(term);

        resp.addHeader("Content-Type", "application/json");
        
        ObjectMapper jackson = new ObjectMapper();
        jackson.writeValue(resp.getOutputStream(), ngdNeighbours);
    }

    static Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

}