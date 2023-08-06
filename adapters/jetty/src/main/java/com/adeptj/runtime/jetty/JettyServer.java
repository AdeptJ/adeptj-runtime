package com.adeptj.runtime.jetty;

import com.adeptj.runtime.jetty.handler.ContextPathHandler;
import com.adeptj.runtime.jetty.handler.HealthCheckHandler;
import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.kernel.exception.ServerException;
import com.typesafe.config.Config;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.eclipse.jetty.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class JettyServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private static final String PARAM_RESOURCE_BASE = "resourceBase";

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.JETTY;
    }

    @Override
    public String getUnderlyingServerInfo() {
        return "Eclipse " + this.getRuntime() + "/" + Server.getVersion();
    }

    @Override
    public void start(ServletDeployment deployment, Config appConfig, String[] args) throws Exception {
        this.jetty = this.initJetty(appConfig);
        this.context = new ServletContextHandler(SESSIONS | SECURITY);
        this.context.setContextPath(appConfig.getString("jetty.context.path"));
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context, this.getUserManager(), appConfig);
        new ErrorHandlerConfigurer().configure(this.context, appConfig);
        this.jetty.setHandler(this.getRootHandler(this.context, appConfig));
        if (Boolean.getBoolean("adeptj.rt.jetty.req.logging")) {
            this.jetty.setRequestLog(new CustomRequestLog());
        }
        this.jetty.start();
    }

    private Server initJetty(Config appConfig) {
        Server jetty = new Server(this.getQueuedThreadPool(appConfig));
        HttpConnectionFactory connectionFactory = new HttpConnectionFactory(this.getHttpConfiguration(appConfig));
        ServerConnector connector = new ServerConnector(jetty, connectionFactory);
        connector.setPort(this.resolvePort(appConfig));
        connector.setIdleTimeout(appConfig.getLong("jetty.connector.idle-timeout"));
        jetty.addConnector(connector);
        return jetty;
    }

    private QueuedThreadPool getQueuedThreadPool(Config appConfig) {
        int minThreads = appConfig.getInt("jetty.qtp.min-threads");
        int maxThreads = appConfig.getInt("jetty.qtp.max-threads");
        int idleTimeout = appConfig.getInt("jetty.qtp.idle-timeout");
        return new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
    }

    private HttpConfiguration getHttpConfiguration(Config appConfig) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(appConfig.getInt("jetty.http.output-buffer-size"));
        httpConfig.setRequestHeaderSize(appConfig.getInt("jetty.http.request-header-size"));
        httpConfig.setResponseHeaderSize(appConfig.getInt("jetty.http.response-header-size"));
        httpConfig.setSendServerVersion(appConfig.getBoolean("jetty.http.send-server-version"));
        httpConfig.setSendDateHeader(appConfig.getBoolean("jetty.http.send-date-header"));
        return httpConfig;
    }

    private Handler getRootHandler(ServletContextHandler rootContext, Config appConfig) {
        // Sequence will be - HealthCheckHandler -> ContextPathHandler -> ServletContextHandler
        ContextPathHandler contextPathHandler = new ContextPathHandler();
        contextPathHandler.setHandler(new HealthCheckHandler());
        rootContext.insertHandler(contextPathHandler);
        String defaultServletPath = appConfig.getString("jetty.context.default-servlet-path");
        ServletHolder defaultServlet = rootContext.addServlet(DefaultServlet.class, defaultServletPath);
        defaultServlet.setAsyncSupported(true);
        defaultServlet.setInitParameter(PARAM_RESOURCE_BASE, this.getResourceBase(appConfig));
        return rootContext;
    }

    private String getResourceBase(Config appConfig) {
        String resourceBase;
        String basePath = appConfig.getString("jetty.context.static-resources-base-path");
        URL webappRoot = this.getClass().getResource(basePath);
        if (webappRoot == null) { // Fallback
            try (Resource resource = Resource.newClassPathResource(basePath)) {
                resourceBase = resource.getName();
                LOGGER.info("Static resource base resolved using Jetty Resource.newClassPathResource()");
            }
        } else {
            resourceBase = webappRoot.toExternalForm();
        }
        LOGGER.info("Static resource base resolved to: [{}]", resourceBase);
        return resourceBase;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void postStart() {
        try {
            this.jetty.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        this.jetty.stop();
    }

    @Override
    protected void doRegisterServlet(ServletInfo info) {
        this.context.addServlet(new ServletHolder(info.servletName(), info.servletClass()), info.path());
    }

    @Override
    protected void doRegisterFilter(FilterInfo info) {
        // NOP
    }

    @Override
    public void addServletContextAttribute(String name, Object value) {
        this.context.getServletContext().setAttribute(name, value);
    }
}