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
import com.typesafe.config.Config;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.List;

import static org.eclipse.jetty.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class JettyServer extends AbstractServer {

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.JETTY;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
        Config config = ConfigProvider.getInstance().getReferenceConfig();
        int minThreads = config.getInt("jetty.qtp.minThreads");
        int maxThreads = config.getInt("jetty.qtp.maxThreads");
        int idleTimeout = config.getInt("jetty.qtp.idleTimeout");
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
        connector.addBean(new TimingHttpChannelListener());
        this.jetty.addConnector(connector);
        this.context = new ServletContextHandler(SESSIONS | SECURITY);
        this.context.setContextPath("/");
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context, this.getUserManager());
        new ErrorHandlerConfigurer().configure(this.context);
        this.jetty.setHandler(this.createRootHandler(this.context));
        try {
            this.jetty.start();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Handler createRootHandler(ServletContextHandler servletContextHandler) {
        servletContextHandler.insertHandler(new ContextPathHandler());
        servletContextHandler.insertHandler(new HealthCheckHandler());
        GzipHandler gzipHandler = new GzipHandler();
        gzipHandler.setHandler(new ContextHandlerCollection(servletContextHandler, this.createStaticContextHandler()));
        return gzipHandler;
    }

    private ContextHandler createStaticContextHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        ContextHandler staticResourceContext = new ContextHandler();
        staticResourceContext.setContextPath("/static");
        staticResourceContext.setBaseResource(Resource.newClassPathResource("/WEB-INF/static"));
        staticResourceContext.setHandler(resourceHandler);
        return staticResourceContext;
    }

    @Override
    public void stop() {
        try {
            super.preStop();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
        try {
            this.jetty.stop();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doRegisterServlet(ServletInfo info) {
        this.context.addServlet(new ServletHolder(info.getServletName(), info.getServletClass()), info.getPath());
    }

    @Override
    protected void doRegisterFilter(FilterInfo info) {

    }

    @Override
    public void registerErrorPages(List<Integer> errorCodes) {

    }

    @Override
    public void addServletContextAttribute(String name, Object value) {
        this.context.getServletContext().setAttribute(name, value);
    }
}