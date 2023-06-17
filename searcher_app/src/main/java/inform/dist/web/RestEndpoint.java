package inform.dist.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RestEndpoint {

    @Autowired
    NgdNeighboursService ngdNeighboursService;

    @GetMapping("/endpoint")
    protected List<Map<String, Object>> doGet(HttpServletRequest req, HttpServletResponse resp) {

        String term = req.getParameter("term");
        if (term == null)
            throw new IllegalArgumentException("must provide 'term' param");

        int size = 40;
        try {
            final String strsize = req.getParameter("size");
            size = Integer.parseInt(strsize);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

//        AppConf applicationContext = AppConf.getInstance(req.getServletContext());
//        final NgdNeighboursService ngdNeighboursService = applicationContext.getNgdNeighboursService();
        final List<Map<String, Object>> ngdNeighbours = ngdNeighboursService.getNgdNeighbours(term, size);

        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Content-Type", "application/json");
        
        // resp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        return ngdNeighbours;
//        ObjectMapper jackson = new ObjectMapper();
//        jackson.writeValue(resp.getOutputStream(), ngdNeighbours);
    }

    static Logger LOG = LoggerFactory.getLogger(RestEndpoint.class);

}