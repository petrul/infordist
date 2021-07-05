package inform.dist.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {
     private Server server;
     
    public static void main(String[] args) {
        try {
            new JettyServer().start();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public void start() throws Exception {
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setExtractWAR(false);
//        context.setDescriptor("WEB-INF/web.xml");
//        context.setResourceBase("../webapp/");
        context.setConfigurationDiscovered(true);

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler(context);

        Server server = new Server(8080);
        server.setHandler(handlerList);
        server.start();
    }

     public void start2() throws Exception {
         server = new Server();
         ServerConnector connector = new ServerConnector(server);
         connector.setPort(8080);
         server.setConnectors(new Connector[] {connector});
     }
}