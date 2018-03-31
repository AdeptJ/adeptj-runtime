/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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

package com.adeptj.runtime.server;

import com.adeptj.runtime.common.DefaultExecutorService;
import com.adeptj.runtime.common.Environment;
import com.adeptj.runtime.common.IOUtils;
import com.adeptj.runtime.common.SslContextFactory;
import com.adeptj.runtime.common.Stoppable;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.common.Verb;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.core.ContainerInitializer;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.servlet.AuthServlet;
import com.adeptj.runtime.servlet.CryptoServlet;
import com.adeptj.runtime.servlet.ErrorPageServlet;
import com.adeptj.runtime.servlet.ToolsServlet;
import com.adeptj.runtime.tools.logging.LogbackManager;
import com.adeptj.runtime.websocket.ServerLogsWebSocket;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.Version;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.RequestBufferingHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.CrawlerSessionManagerConfig;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletSessionConfig;
import io.undertow.util.HttpString;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.adeptj.runtime.common.Constants.ARG_OPEN_CONSOLE;
import static com.adeptj.runtime.common.Constants.BANNER_TXT;
import static com.adeptj.runtime.common.Constants.CONTEXT_PATH;
import static com.adeptj.runtime.common.Constants.DEPLOYMENT_NAME;
import static com.adeptj.runtime.common.Constants.DIR_ADEPTJ_RUNTIME;
import static com.adeptj.runtime.common.Constants.DIR_DEPLOYMENT;
import static com.adeptj.runtime.common.Constants.HEADER_SERVER;
import static com.adeptj.runtime.common.Constants.HEADER_X_POWERED_BY;
import static com.adeptj.runtime.common.Constants.KEY_ALLOWED_METHODS;
import static com.adeptj.runtime.common.Constants.KEY_HEADER_SERVER;
import static com.adeptj.runtime.common.Constants.KEY_HOST;
import static com.adeptj.runtime.common.Constants.KEY_HTTP;
import static com.adeptj.runtime.common.Constants.KEY_MAX_CONCURRENT_REQS;
import static com.adeptj.runtime.common.Constants.KEY_PORT;
import static com.adeptj.runtime.common.Constants.KEY_REQ_BUFF_MAX_BUFFERS;
import static com.adeptj.runtime.common.Constants.OSGI_CONSOLE_URL;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.SYS_PROP_SERVER_PORT;
import static com.adeptj.runtime.common.Constants.TOOLS_CRYPTO_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_DASHBOARD_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_LOGIN_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_LOGOUT_URI;
import static com.adeptj.runtime.server.ServerConstants.AUTH_SERVLET;
import static com.adeptj.runtime.server.ServerConstants.CRYPTO_SERVLET;
import static com.adeptj.runtime.server.ServerConstants.DEFAULT_WAIT_TIME;
import static com.adeptj.runtime.server.ServerConstants.ERROR_PAGE_SERVLET;
import static com.adeptj.runtime.server.ServerConstants.KEY_AJP;
import static com.adeptj.runtime.server.ServerConstants.KEY_AUTH_ROLES;
import static com.adeptj.runtime.server.ServerConstants.KEY_CHANGE_SESSIONID_ON_LOGIN;
import static com.adeptj.runtime.server.ServerConstants.KEY_DEFAULT_ENCODING;
import static com.adeptj.runtime.server.ServerConstants.KEY_ERROR_PAGES;
import static com.adeptj.runtime.server.ServerConstants.KEY_HTTPS;
import static com.adeptj.runtime.server.ServerConstants.KEY_HTTP_ONLY;
import static com.adeptj.runtime.server.ServerConstants.KEY_IGNORE_FLUSH;
import static com.adeptj.runtime.server.ServerConstants.KEY_INVALIDATE_SESSION_ON_LOGOUT;
import static com.adeptj.runtime.server.ServerConstants.KEY_KEYSTORE;
import static com.adeptj.runtime.server.ServerConstants.KEY_MULTIPART_FILE_LOCATION;
import static com.adeptj.runtime.server.ServerConstants.KEY_MULTIPART_FILE_SIZE_THRESHOLD;
import static com.adeptj.runtime.server.ServerConstants.KEY_MULTIPART_MAX_FILE_SIZE;
import static com.adeptj.runtime.server.ServerConstants.KEY_MULTIPART_MAX_REQUEST_SIZE;
import static com.adeptj.runtime.server.ServerConstants.KEY_SECURED_URLS;
import static com.adeptj.runtime.server.ServerConstants.KEY_SECURED_URLS_ALLOWED_METHODS;
import static com.adeptj.runtime.server.ServerConstants.KEY_SESSION_TIMEOUT;
import static com.adeptj.runtime.server.ServerConstants.KEY_USE_CACHED_AUTH_MECHANISM;
import static com.adeptj.runtime.server.ServerConstants.KEY_WORKER_OPTIONS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WORKER_TASK_CORE_THREADS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WORKER_TASK_MAX_THREADS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_BUFFER_SIZE;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_IO_THREADS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_TASK_CORE_THREADS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_TASK_MAX_THREADS;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_TCP_NO_DELAY;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_USE_DIRECT_BUFFER;
import static com.adeptj.runtime.server.ServerConstants.KEY_WS_WEB_SOCKET_OPTIONS;
import static com.adeptj.runtime.server.ServerConstants.REALM;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_CHECK_PORT;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_ENABLE_AJP;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_ENABLE_HTTP2;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_ENABLE_REQ_BUFF;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_MAX_CONCUR_REQ;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_REQ_BUFF_MAX_BUFFERS;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_SESSION_TIMEOUT;
import static com.adeptj.runtime.server.ServerConstants.SYS_PROP_SHUTDOWN_WAIT_TIME;
import static com.adeptj.runtime.server.ServerConstants.SYS_TASK_THREAD_MULTIPLIER;
import static com.adeptj.runtime.server.ServerConstants.TOOLS_DASHBOARD_URL;
import static com.adeptj.runtime.server.ServerConstants.TOOLS_ERROR_URL;
import static com.adeptj.runtime.server.ServerConstants.TOOLS_SERVLET;
import static com.adeptj.runtime.server.ServerConstants.WORKER_TASK_THREAD_MULTIPLIER;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.ATTRIBUTE_NAME;
import static javax.servlet.http.HttpServletRequest.FORM_AUTH;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

/**
 * Provisions the Undertow Web Server, start OSGi framework and much more.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Server implements Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private Undertow undertow;

    private DeploymentManager deploymentManager;

    private GracefulShutdownHandler rootHandler;

    private WeakReference<Config> cfgReference;

    public void start(Map<String, String> commands) throws ServletException {
        this.cfgReference = new WeakReference<>(Configs.DEFAULT.undertow());
        Config httpConf = Objects.requireNonNull(this.cfgReference.get()).getConfig(KEY_HTTP);
        int httpPort = this.handlePortAvailability(httpConf);
        LOGGER.info("Starting AdeptJ Runtime @port: [{}]", httpPort);
        this.printBanner();
        this.deploymentManager = Servlets.defaultContainer().addDeployment(this.deploymentInfo());
        this.deploymentManager.deploy();
        this.rootHandler = this.rootHandler(this.deploymentManager.start());
        this.undertow = this.enableAJP(this.enableHttp2(ServerOptions.build(this.workerOptions(Undertow.builder()),
                Objects.requireNonNull(this.cfgReference.get()))))
                .addHttpListener(httpPort, httpConf.getString(KEY_HOST))
                .setHandler(this.rootHandler)
                .build();
        this.undertow.start();
        this.launchBrowser(commands, httpPort);
        if (!Environment.isServerConfFileExists()) {
            this.createServerConfFile();
        }
    }

    /**
     * Does graceful server shutdown, this first cleans up the deployment and then stops Undertow server.
     */
    @Override
    public void stop() {
        long startTime = System.nanoTime();
        LOGGER.info("Stopping AdeptJ Runtime!!");
        try {
            this.gracefulShutdown();
            this.deploymentManager.stop();
            this.deploymentManager.undeploy();
            this.undertow.stop();
            LOGGER.info("AdeptJ Runtime stopped in [{}] ms!!", Times.elapsedMillis(startTime));
            DefaultExecutorService.INSTANCE.shutdown();
        } catch (Throwable ex) { // NOSONAR
            LOGGER.error("Exception while stopping AdeptJ Runtime!!", ex);
        } finally {
            // Let the Logback cleans up it's state.
            LogbackManager.INSTANCE.getLoggerContext().stop();
        }
    }

    private void gracefulShutdown() {
        try {
            this.rootHandler.shutdown();
            if (this.rootHandler.awaitShutdown(Long.getLong(SYS_PROP_SHUTDOWN_WAIT_TIME, DEFAULT_WAIT_TIME))) {
                LOGGER.info("Completed remaining requests successfully!!");
            }
        } catch (InterruptedException ie) {
            LOGGER.error("Error while waiting for pending request to complete!!", ie);
            // SONAR - "InterruptedException" should not be ignored
            // Can't really rethrow it as we are yet to stop the server and anyway it's a shutdown hook
            // and JVM itself will be shutting down shortly.
            Thread.currentThread().interrupt();
        }
    }

    private void printBanner() {
        try (InputStream stream = Server.class.getResourceAsStream(BANNER_TXT)) {
            LOGGER.info(IOUtils.toString(stream)); // NOSONAR
        } catch (IOException ex) {
            // Just log it, its not critical.
            LOGGER.error("Exception while printing server banner!!", ex);
        }
    }

    private void launchBrowser(Map<String, String> arguments, int httpPort) {
        if (Boolean.parseBoolean(arguments.get(ARG_OPEN_CONSOLE))) {
            try {
                Environment.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL, httpPort)));
            } catch (IOException ex) {
                // Just log it, its okay if browser is not launched.
                LOGGER.error("Exception while launching browser!!", ex);
            }
        }
    }

    private void createServerConfFile() {
        try (InputStream stream = Server.class.getResourceAsStream("/reference.conf")) {
            Files.write(Paths.get(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, SERVER_CONF_FILE),
                    IOUtils.toBytes(stream), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOGGER.error("Exception while creating server conf file!!", ex);
        }
    }

    private Builder workerOptions(Builder builder) {
        if (Environment.isProd()) {
            Config workerOptions = Objects.requireNonNull(this.cfgReference.get()).getConfig(KEY_WORKER_OPTIONS);
            // defaults to 64
            int cfgCoreTaskThreads = workerOptions.getInt(KEY_WORKER_TASK_CORE_THREADS);
            int calcCoreTaskThreads = Runtime.getRuntime().availableProcessors() * WORKER_TASK_THREAD_MULTIPLIER;
            builder.setWorkerOption(Options.WORKER_TASK_CORE_THREADS, calcCoreTaskThreads > cfgCoreTaskThreads
                    ? calcCoreTaskThreads : cfgCoreTaskThreads);
            // defaults to double of [worker-task-core-threads] i.e 128
            int cfgMaxTaskThreads = workerOptions.getInt(KEY_WORKER_TASK_MAX_THREADS);
            int calcMaxTaskThreads = calcCoreTaskThreads * SYS_TASK_THREAD_MULTIPLIER;
            builder.setWorkerOption(Options.WORKER_TASK_MAX_THREADS, calcMaxTaskThreads > cfgMaxTaskThreads
                    ? calcMaxTaskThreads : cfgMaxTaskThreads);
            // For a 16 core system, number of worker task core and max threads will be.
            // 1. core task thread: 128 (16[cores] * 8)
            // 2. max task thread: 128 * 2 = 256
            // Default settings would have set the following.
            // 1. core task thread: 128 (16[cores] * 8)
            // 2. max task thread: 128 (Same as core task thread)
            LOGGER.info("Undertow Worker Options optimized for AdeptJ Runtime [PROD] mode.");
        }
        return builder;
    }

    private Builder enableHttp2(Builder undertowBuilder) {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_HTTP2)) {
            Config httpsConf = Objects.requireNonNull(this.cfgReference.get()).getConfig(KEY_HTTPS);
            int httpsPort = httpsConf.getInt(KEY_PORT);
            if (!Environment.useProvidedKeyStore()) {
                System.setProperty("javax.net.ssl.keyStore", httpsConf.getString(KEY_KEYSTORE));
                System.setProperty("javax.net.ssl.keyStorePassword", httpsConf.getString("keyStorePwd"));
                System.setProperty("javax.net.ssl.keyPassword", httpsConf.getString("keyPwd"));
                LOGGER.info("HTTP2 enabled @ port: [{}] using bundled KeyStore.", httpsPort);
            }
            undertowBuilder.addHttpsListener(httpsPort, httpsConf.getString(KEY_HOST), SslContextFactory.createSslContext());
        }
        return undertowBuilder;
    }

    private Builder enableAJP(Builder undertowBuilder) {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_AJP)) {
            Config ajpConf = Objects.requireNonNull(this.cfgReference.get()).getConfig(KEY_AJP);
            int ajpPort = ajpConf.getInt(KEY_PORT);
            undertowBuilder.addAjpListener(ajpPort, ajpConf.getString(KEY_HOST));
            LOGGER.info("AJP enabled @ port: [{}]", ajpPort);
        }
        return undertowBuilder;
    }

    private int handlePortAvailability(Config httpConf) {
        Integer port = Integer.getInteger(SYS_PROP_SERVER_PORT);
        if (port == null) {
            LOGGER.warn("No port specified via system property: [{}], using default port: [{}]",
                    SYS_PROP_SERVER_PORT, httpConf.getInt(KEY_PORT));
            port = httpConf.getInt(KEY_PORT);
        }
        // Note: Shall we do it ourselves or let server do it later? Problem may arise in OSGi Framework provisioning
        // as it is being started already and another server start(from same location) will again start new OSGi
        // Framework which may interfere with already started OSGi Framework as the bundle deployment, heap dump,
        // OSGi configurations directory is common, this is unknown at this moment but just to be on safer side doing this.
        if (Boolean.getBoolean(SYS_PROP_CHECK_PORT) && !isPortAvailable(port)) {
            LOGGER.error("Port: [{}] already used, shutting down JVM!!", port);
            // Let the LOGBACK cleans up it's state.
            LogbackManager.INSTANCE.getLoggerContext().stop();
            System.exit(-1); // NOSONAR
        }
        return port;
    }

    private boolean isPortAvailable(int port) {
        boolean portAvailable = false;
        ServerSocket socket = null;
        try (ServerSocketChannel socketChannel = ServerSocketChannel.open()) {
            socket = socketChannel.socket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));
            portAvailable = true;
        } catch (BindException ex) {
            LOGGER.error("BindException while acquiring port: [{}], cause:", port, ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while acquiring port: [{}], cause:", port, ex);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    LOGGER.error("IOException while closing socket!!", ex);
                }
            }
        }
        return portAvailable;
    }

    /**
     * Chaining of Undertow {@link HttpHandler} instances as follows.
     * <p>
     * 1. GracefulShutdownHandler
     * 2. RequestLimitingHandler
     * 3. AllowedMethodsHandler
     * 4. PredicateHandler which resolves to either RedirectHandler or SetHeadersHandler
     * 5. RequestBufferingHandler if request buffering is enabled, wrapped in SetHeadersHandler
     * 5. And Finally ServletInitialHandler
     *
     * @param servletInitialHandler the {@link io.undertow.servlet.handlers.ServletInitialHandler}
     * @return GracefulShutdownHandler as the root handler
     */
    private GracefulShutdownHandler rootHandler(HttpHandler servletInitialHandler) {
        Config cfg = Objects.requireNonNull(this.cfgReference.get());
        Map<HttpString, String> headers = new HashMap<>();
        headers.put(HttpString.tryFromString(HEADER_SERVER), cfg.getString(KEY_HEADER_SERVER));
        if (Environment.isDev()) {
            headers.put(HttpString.tryFromString(HEADER_X_POWERED_BY), Version.getFullVersionString());
        }
        HttpHandler headersHandler = Boolean.getBoolean(SYS_PROP_ENABLE_REQ_BUFF) ?
                new SetHeadersHandler(new RequestBufferingHandler(servletInitialHandler,
                        Integer.getInteger(SYS_PROP_REQ_BUFF_MAX_BUFFERS, cfg.getInt(KEY_REQ_BUFF_MAX_BUFFERS))), headers) :
                new SetHeadersHandler(servletInitialHandler, headers);
        return Handlers.gracefulShutdown(
                new RequestLimitingHandler(Integer.getInteger(SYS_PROP_MAX_CONCUR_REQ, cfg.getInt(KEY_MAX_CONCURRENT_REQS)),
                        new AllowedMethodsHandler(Handlers.predicate(exchange -> CONTEXT_PATH.equals(exchange.getRequestURI()),
                                Handlers.redirect(TOOLS_DASHBOARD_URI), headersHandler), this.allowedMethods(cfg))));
    }

    private Set<HttpString> allowedMethods(Config cfg) {
        return cfg.getStringList(KEY_ALLOWED_METHODS)
                .stream()
                .map(Verb::from)
                .collect(Collectors.toSet());
    }

    private List<ErrorPage> errorPages(Config cfg) {
        return cfg.getObject(KEY_ERROR_PAGES).unwrapped()
                .entrySet()
                .stream()
                .map(e -> Servlets.errorPage(String.valueOf(e.getValue()), Integer.parseInt(e.getKey())))
                .collect(Collectors.toList());
    }

    private ServletContainerInitializerInfo sciInfo() {
        // Since the execution order of StartupAware instances matter that's why a LinkedHashSet.
        // FrameworkLauncher must always be executed first.
        Set<Class<?>> handlesTypes = new LinkedHashSet<>();
        handlesTypes.add(FrameworkLauncher.class);
        handlesTypes.add(DefaultStartupAware.class);
        return new ServletContainerInitializerInfo(ContainerInitializer.class, handlesTypes);
    }

    private SecurityConstraint securityConstraint(Config cfg) {
        return Servlets.securityConstraint()
                .addRolesAllowed(cfg.getStringList(KEY_AUTH_ROLES))
                .addWebResourceCollection(Servlets.webResourceCollection()
                        .addHttpMethods(cfg.getStringList(KEY_SECURED_URLS_ALLOWED_METHODS))
                        .addUrlPatterns(cfg.getStringList(KEY_SECURED_URLS)));
    }

    private List<ServletInfo> servlets() {
        List<ServletInfo> servlets = new ArrayList<>();
        servlets.add(Servlets
                .servlet(ERROR_PAGE_SERVLET, ErrorPageServlet.class)
                .addMapping(TOOLS_ERROR_URL)
                .setAsyncSupported(true));
        servlets.add(Servlets
                .servlet(TOOLS_SERVLET, ToolsServlet.class)
                .addMapping(TOOLS_DASHBOARD_URL)
                .setAsyncSupported(true));
        servlets.add(Servlets
                .servlet(AUTH_SERVLET, AuthServlet.class)
                .addMappings(TOOLS_LOGIN_URI, TOOLS_LOGOUT_URI)
                .setAsyncSupported(true));
        servlets.add(Servlets.servlet(CRYPTO_SERVLET, CryptoServlet.class)
                .addMappings(TOOLS_CRYPTO_URI)
                .setAsyncSupported(true));
        return servlets;
    }

    private MultipartConfigElement defaultMultipartConfig(Config cfg) {
        return Servlets.multipartConfig(cfg.getString(KEY_MULTIPART_FILE_LOCATION),
                cfg.getLong(KEY_MULTIPART_MAX_FILE_SIZE),
                cfg.getLong(KEY_MULTIPART_MAX_REQUEST_SIZE),
                cfg.getInt(KEY_MULTIPART_FILE_SIZE_THRESHOLD));
    }

    private WebSocketDeploymentInfo webSocketDeploymentInfo(Config cfg) {
        Config wsOptions = cfg.getConfig(KEY_WS_WEB_SOCKET_OPTIONS);
        return new WebSocketDeploymentInfo()
                .setWorker(this.webSocketWorker(wsOptions))
                .setBuffers(new DefaultByteBufferPool(wsOptions.getBoolean(KEY_WS_USE_DIRECT_BUFFER),
                        wsOptions.getInt(KEY_WS_BUFFER_SIZE)))
                .addEndpoint(ServerLogsWebSocket.class);
    }

    private XnioWorker webSocketWorker(Config wsOptions) {
        XnioWorker worker = null;
        try {
            worker = Xnio.getInstance().createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, wsOptions.getInt(KEY_WS_IO_THREADS))
                    .set(Options.WORKER_TASK_CORE_THREADS, wsOptions.getInt(KEY_WS_TASK_CORE_THREADS))
                    .set(Options.WORKER_TASK_MAX_THREADS, wsOptions.getInt(KEY_WS_TASK_MAX_THREADS))
                    .set(Options.TCP_NODELAY, wsOptions.getBoolean(KEY_WS_TCP_NO_DELAY))
                    .getMap());
        } catch (IOException ex) {
            LOGGER.error("Can't create XnioWorker!!", ex);
        }
        return worker;
    }

    private int sessionTimeout(Config cfg) {
        return Integer.getInteger(SYS_PROP_SESSION_TIMEOUT, cfg.getInt(KEY_SESSION_TIMEOUT));
    }

    private ServletSessionConfig sessionConfig(Config cfg) {
        return new ServletSessionConfig().setHttpOnly(cfg.getBoolean(KEY_HTTP_ONLY));
    }

    private DeploymentInfo deploymentInfo() {
        Config cfg = Objects.requireNonNull(this.cfgReference.get());
        return Servlets.deployment()
                .setDeploymentName(DEPLOYMENT_NAME)
                .setContextPath(CONTEXT_PATH)
                .setClassLoader(Server.class.getClassLoader())
                .addServletContainerInitalizer(this.sciInfo())
                .setIgnoreFlush(cfg.getBoolean(KEY_IGNORE_FLUSH))
                .setDefaultEncoding(cfg.getString(KEY_DEFAULT_ENCODING))
                .setDefaultSessionTimeout(this.sessionTimeout(cfg))
                .setChangeSessionIdOnLogin(cfg.getBoolean(KEY_CHANGE_SESSIONID_ON_LOGIN))
                .setInvalidateSessionOnLogout(cfg.getBoolean(KEY_INVALIDATE_SESSION_ON_LOGOUT))
                .setIdentityManager(new SimpleIdentityManager(cfg))
                .setUseCachedAuthenticationMechanism(cfg.getBoolean(KEY_USE_CACHED_AUTH_MECHANISM))
                .setLoginConfig(Servlets.loginConfig(FORM_AUTH, REALM, TOOLS_LOGIN_URI, TOOLS_LOGIN_URI))
                .addSecurityConstraint(this.securityConstraint(cfg))
                .addServlets(this.servlets())
                .addErrorPages(this.errorPages(cfg))
                .setDefaultMultipartConfig(this.defaultMultipartConfig(cfg))
                .addInitialHandlerChainWrapper(new ServletInitialHandlerWrapper())
                .addServletContextAttribute(ATTRIBUTE_NAME, this.webSocketDeploymentInfo(cfg))
                .setServletSessionConfig(this.sessionConfig(cfg))
                .setCrawlerSessionManagerConfig(new CrawlerSessionManagerConfig());
    }
}