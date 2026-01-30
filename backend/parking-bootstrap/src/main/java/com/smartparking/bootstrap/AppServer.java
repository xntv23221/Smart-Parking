package com.smartparking.bootstrap;

import com.smartparking.bootstrap.config.RootConfig;
import com.smartparking.bootstrap.config.WebMvcConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppServer {
    public static void main(String[] args) throws Exception {
        int port = resolvePort();

        Server server = new Server(port);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(RootConfig.class, WebMvcConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        servletHolder.setInitOrder(1);
        contextHandler.addServlet(servletHolder, "/");

        server.setHandler(contextHandler);
        server.start();
        server.join();
    }

    private static int resolvePort() {
        String sys = System.getProperty("server.port");
        if (sys != null && !sys.isBlank()) {
            return Integer.parseInt(sys);
        }
        String env = System.getenv("SERVER_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env);
        }
        return 8080;
    }
}

