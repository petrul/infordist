package inform.dist.web;

import inform.dist.Constants;
import inform.dist.web.beans.TermService;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.indexer.WikipediaSnowballAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class NgdNeighboursService {

    protected final TermService termService;

    public NgdNeighboursService(TermService termService) {
        this.termService = termService;
    }

    public List<Map<String, Object>> getNgdNeighbours(String term) {
        String analyzedTerm = this.analyze(term);
        return this.termService.getNgdNeighbours(analyzedTerm);
    }

    String analyze(String term) {
        LOG.info("analyze term:" + term);
        byte[] bytes = term.getBytes(Charset.forName("latin1"));
        String utf8term = new String(bytes, Charset.forName("utf-8"));
        WikipediaSnowballAnalyzer analyzer = new WikipediaSnowballAnalyzer("English");
        Token tk = new Token();
        TokenStream stream = analyzer.tokenStream(Constants.FIELD_TEXT, new StringReader(utf8term));
        try {
            if (stream.next(tk) == null) {
//                FacesContext.getCurrentInstance().addMessage("problem", new FacesMessage("problem, man"));
                throw new IllegalStateException("problem, man");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String res = tk.term();
        return res;
    }

    static Logger LOG = LoggerFactory.getLogger(ApplicationContext.class);
}
