package inform.dist.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

/**
 * i don't use this
 * @author dadi
 *
 */
public class CustomCharacterEncodingFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {
        //No-op
    }

    public void doFilter(
    			ServletRequest request, 
    			ServletResponse response, 
    			FilterChain chain) 
                             throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
//        LOG.info("term in filter:" + request.getParameter("term"));
        
        chain.doFilter(request, response);
        
        
    }

    public void destroy() {
        //No-op
    }
    Logger LOG = Logger.getLogger(CustomCharacterEncodingFilter.class);
}
