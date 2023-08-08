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
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
        this.context = this.initServletContextHandler(appConfig);
        // Handler sequence will be -  ContextPathHandler -> HealthCheckHandler -> SessionHandler -> SecurityHandler
        HandlerList chain = new HandlerList(new ContextPathHandler(), new HealthCheckHandler(), this.context);
        this.jetty.setHandler(chain);
        // Servlet deployment
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(sciInfo.getSciInstance(), sciInfo.getHandleTypesArray());
        this.registerServlets(deployment.getServletInfos());
        this.configureDefaultServlet(appConfig);
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

    /**
     * Initializes the {@link ServletContextHandler}
     *
     * @param appConfig the application config.
     * @return a fully configured {@link ServletContextHandler}
     */
    private ServletContextHandler initServletContextHandler(Config appConfig) {
        ServletContextHandler contextHandler = new ServletContextHandler(SESSIONS | SECURITY);
        contextHandler.setContextPath(appConfig.getString("jetty.context.path"));
        Config commonConfig = appConfig.getConfig("main.common");
        // SecurityHandler - for Servlet container security
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        new SecurityConfigurer().configure(securityHandler, this.getUserManager(), commonConfig);
        contextHandler.setSecurityHandler(securityHandler);
        // SessionHandler
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setHttpOnly(commonConfig.getBoolean("session-cookie-httpOnly"));
        sessionHandler.setMaxInactiveInterval(commonConfig.getInt("session-timeout"));
        contextHandler.setSessionHandler(sessionHandler);
        // ErrorHandler
        String errorHandlerPath = commonConfig.getString("error-handler-path");
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        commonConfig.getIntList("error-handler-codes")
                .forEach(value -> errorHandler.addErrorPage(value, errorHandlerPath));
        contextHandler.setErrorHandler(errorHandler);
        return contextHandler;
    }

    private void configureDefaultServlet(Config appConfig) {
        String resourceBase;
        String basePath = appConfig.getString("jetty.context.static-resources-base-path");
        URL webappRoot = this.getClass().getResource(basePath);
        if (webappRoot == null) {
            throw new IllegalStateException("Jetty could not be started because there are multiple or no" +
                    " adeptj-runtime-x.x.x.jar file present at classpath which has the static resources!!");
        } else {
            resourceBase = webappRoot.toExternalForm();
        }
        LOGGER.info("Static resource base path resolved to: [{}]", resourceBase);
        String defaultServletPath = appConfig.getString("jetty.context.default-servlet-path");
        ServletHolder defaultServlet = this.context.addServlet(DefaultServlet.class, defaultServletPath);
        defaultServlet.setAsyncSupported(true);
        defaultServlet.setInitParameter(PARAM_RESOURCE_BASE, resourceBase);
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