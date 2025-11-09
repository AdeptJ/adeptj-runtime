/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
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
import org.eclipse.jetty.compression.server.CompressionHandler;
import org.eclipse.jetty.ee11.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee11.servlet.ResourceServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.servlet.SessionHandler;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.GracefulHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static com.adeptj.runtime.kernel.ServerRuntime.JETTY;
import static org.eclipse.jetty.ee11.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.ee11.servlet.ServletContextHandler.SESSIONS;

/**
 * AdeptJ Runtime server implementation using Jetty.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class JettyServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private static final String PARAM_BASE_RESOURCE = "baseResource";

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerRuntime getRuntime() {
        return JETTY;
    }

    @Override
    public void start(ServletDeployment deployment, Config appConfig, String[] args) throws Exception {
        // Check for base resource upfront to avoid any issues later.
        String baseResource = this.getBaseResource(appConfig);
        this.jetty = this.initJetty(appConfig);
        this.context = this.initServletContextHandler(appConfig);
        // Handler sequence will be - GracefulHandler ->
        // ContextPathHandler -> HealthCheckHandler -> GzipHandler -> SessionHandler -> SecurityHandler
        this.jetty.setHandler(new Handler.Sequence(new GracefulHandler(), new ContextPathHandler(),
                new HealthCheckHandler(),
                new CompressionHandler(this.context)));
        // Servlet deployment
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(sciInfo.getSciInstance(), sciInfo.getHandleTypesArray());
        this.registerServlets(deployment.getServletInfos());
        this.configureResourceServlet(appConfig, baseResource);
        if (Boolean.getBoolean("adeptj.rt.jetty.req.logging")) {
            this.jetty.setRequestLog(new CustomRequestLog());
        }
        this.jetty.start();
    }

    private Server initJetty(Config appConfig) {
        Server server = new Server(this.getQueuedThreadPool(appConfig));
        server.setStopTimeout(10_000);
        HttpConfiguration httpConfiguration = this.getHttpConfiguration(appConfig);
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);
        // This will enable h2c
        HTTP2CServerConnectionFactory h2cConnectionFactory = new HTTP2CServerConnectionFactory(httpConfiguration);
        ServerConnector connector = new ServerConnector(server, httpConnectionFactory, h2cConnectionFactory);
        connector.setPort(this.getPort(appConfig));
        connector.setIdleTimeout(appConfig.getLong("jetty.connector.idle-timeout"));
        server.addConnector(connector);
        return server;
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
        httpConfig.setRelativeRedirectAllowed(appConfig.getBoolean("jetty.http.relative-redirect-allowed"));
        return httpConfig;
    }

    /**
     * Initializes the {@link ServletContextHandler}
     *
     * @param appConfig the application config.
     * @return a fully configured {@link ServletContextHandler}
     */
    private ServletContextHandler initServletContextHandler(Config appConfig) {
        String contextPath = appConfig.getString("jetty.context.path");
        ServletContextHandler contextHandler = new ServletContextHandler(contextPath, SESSIONS | SECURITY);
        Config commonConfig = appConfig.getConfig("main.common");
        // SecurityHandler - for Servlet container based security
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        new SecurityConfigurer().configure(securityHandler, this.getUserManager(), commonConfig);
        contextHandler.setSecurityHandler(securityHandler);
        // SessionHandler
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setHttpOnly(commonConfig.getBoolean("session-cookie-httpOnly"));
        sessionHandler.setMaxInactiveInterval(commonConfig.getInt("session-timeout"));
        contextHandler.setSessionHandler(sessionHandler);
        // ErrorHandler
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        String errorHandlerPath = commonConfig.getString("error-handler-path");
        commonConfig.getIntList("error-handler-codes")
                .forEach(value -> errorHandler.addErrorPage(value, errorHandlerPath));
        contextHandler.setErrorHandler(errorHandler);
        return contextHandler;
    }

    private void configureResourceServlet(Config appConfig, String baseResource) {
        ServletHolder holderDef = new ServletHolder("AdeptJStaticResourceServlet", ResourceServlet.class);
        holderDef.setAsyncSupported(true);
        holderDef.setInitParameter(PARAM_BASE_RESOURCE, baseResource);
        this.context.addServlet(holderDef, appConfig.getString("jetty.context.resource-servlet-path"));
    }

    private String getBaseResource(Config appConfig) {
        String baseResource;
        String basePath = appConfig.getString("jetty.context.static-resources-base-path");
        URL webappRoot = this.getClass().getResource(basePath);
        if (webappRoot == null) {
            throw new IllegalStateException("Jetty could not be started because there are multiple or no" +
                    " adeptj-runtime-x.x.x.jar file present at classpath which has the static resources!!");
        }
        baseResource = webappRoot.toExternalForm();
        LOGGER.info("Jetty static resource base path resolved to: [{}]", baseResource);
        return baseResource;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void postStart() {
        try {
            this.jetty.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServerException(ex);
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

    @Override
    public String toString() {
        return "Eclipse " + this.getRuntime() + "/" + Server.getVersion();
    }
}