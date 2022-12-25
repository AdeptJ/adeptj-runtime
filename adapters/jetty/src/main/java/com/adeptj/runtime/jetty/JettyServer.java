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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setRequestHeaderSize(8192);
        httpConfig.setResponseHeaderSize(8192);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(true);
        ServerConnector connector = new ServerConnector(this.jetty, new HttpConnectionFactory(httpConfig));
        connector.setPort(this.resolvePort(config));
        connector.setIdleTimeout(30000);
        this.jetty.addConnector(connector);
        this.context = new ServletContextHandler(SESSIONS | SECURITY);
        this.context.setContextPath("/");
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context, this.getUserManager(), config);
        new ErrorHandlerConfigurer().configure(this.context, config);
        this.jetty.setHandler(this.createRootHandler(this.context));
        try {
            this.jetty.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeInitializationException(e);
        }
    }

    private Handler createRootHandler(ServletContextHandler rootContext) {
        ContextPathHandler contextPathHandler = new ContextPathHandler();
        contextPathHandler.setHandler(new HealthCheckHandler());
        rootContext.insertHandler(contextPathHandler);
        return new ContextHandlerCollection(rootContext, this.createStaticResourcesContextHandler());
    }

    private ContextHandler createStaticResourcesContextHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        ContextHandler staticResourcesContextHandler = new ContextHandler();
        staticResourcesContextHandler.setContextPath("/static");
        staticResourcesContextHandler.setBaseResource(Resource.newClassPathResource("/webapp/static"));
        staticResourcesContextHandler.setHandler(resourceHandler);
        return staticResourcesContextHandler;
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