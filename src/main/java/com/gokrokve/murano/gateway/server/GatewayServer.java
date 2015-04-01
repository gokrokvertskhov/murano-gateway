package com.gokrokve.murano.gateway.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class GatewayServer {
    public static void main(String[] args) throws Exception
    {
        String jetty_home = System.getProperty("jetty.home",".");
 
        Server server = new Server(8080);
 
        WebAppContext webapp = new WebAppContext();
        webapp.setDescriptor("main/webapp/WEB-INF/web.xml");
        webapp.setResourceBase("main");
        webapp.setContextPath("/*");
        //webapp.setWar(jetty_home+"/target/murano-gateway-1.0.war");
        server.setHandler(webapp);
 
        server.start();
        server.join();
    }

}
