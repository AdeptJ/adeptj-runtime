package com.adeptj.runtime.jetty;

import com.adeptj.runtime.jetty.handler.ContextPathHandler;
import com.adeptj.runtime.jetty.handler.HealthCheckHandler;
import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.adeptj.runtime.kernel.exception.ServerException;
import com.typesafe.config.Config;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

import static org.eclipse.jetty.server.CustomRequestLog.EXTENDED_NCSA_FORMAT;
import static org.eclipse.jetty.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class JettyServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.JETTY;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
        Config config = ConfigProvider.getInstance().getApplicationConfig();
        int minThreads = config.getInt("jetty.qtp.min-threads");
        int maxThreads = config.getInt("jetty.qtp.max-threads");
        int idleTimeout = config.getInt("jetty.qtp.idle-timeout");
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        this.jetty = new Server(threadPool);
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(config.getInt("jetty.http.output-buffer-size"));
        httpConfig.setRequestHeaderSize(config.getInt("jetty.http.request-header-size"));
        httpConfig.setResponseHeaderSize(config.getInt("jetty.http.response-header-size"));
        httpConfig.setSendServerVersion(config.getBoolean("jetty.http.send-server-version"));
        httpConfig.setSendDateHeader(config.getBoolean("jetty.http.send-date-header"));
        ServerConnector connector = new ServerConnector(this.jetty, new HttpConnectionFactory(httpConfig));
        connector.setPort(this.resolvePort(config));
        connector.setIdleTimeout(config.getLong("jetty.connector.idle-timeout"));
        this.jetty.addConnector(connector);
        this.context = new ServletContextHandler(SESSIONS | SECURITY);
        this.context.setContextPath(config.getString("jetty.context.path"));
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context, this.getUserManager(), config);
        new ErrorHandlerConfigurer().configure(this.context, config);
        this.jetty.setHandler(this.createRootHandler(this.context, config));
        if (Boolean.getBoolean("adeptj.rt.jetty.req.logging")) {
            this.jetty.setRequestLog(new CustomRequestLog(new Slf4jRequestLogWriter(), EXTENDED_NCSA_FORMAT));
        }
        try {
            this.jetty.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeInitializationException(e);
        }
    }

    private Handler createRootHandler(ServletContextHandler rootContext, Config appConfig) {
        ContextPathHandler contextPathHandler = new ContextPathHandler();
        contextPathHandler.setHandler(new HealthCheckHandler());
        rootContext.insertHandler(contextPathHandler);
        String defaultServletPath = appConfig.getString("jetty.context.default-servlet-path");
        ServletHolder defaultServlet = rootContext.addServlet(DefaultServlet.class, defaultServletPath);
        defaultServlet.setAsyncSupported(true);
        defaultServlet.setInitParameter("resourceBase", this.getResourceBase(appConfig));
        return rootContext;
    }

    private String getResourceBase(Config appConfig) {
        String resourceBase;
        String basePath = appConfig.getString("jetty.context.static-resources-base-path");
        URL webappRoot = this.getClass().getResource(basePath);
        if (webappRoot == null) {
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
        this.context.addServlet(new ServletHolder(info.getServletName(), info.getServletClass()), info.getPath());
    }

    @Override
    protected void doRegisterFilter(FilterInfo info) {
        // NOOP
    }

    @Override
    public void registerErrorPages(List<Integer> errorCodes) {
        // NOOP
    }

    @Override
    public void addServletContextAttribute(String name, Object value) {
        this.context.getServletContext().setAttribute(name, value);
    }
}