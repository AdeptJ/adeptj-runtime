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

import com.adeptj.runtime.common.Environment;
import com.adeptj.runtime.common.IOUtils;
import com.adeptj.runtime.common.SslContextFactory;
import com.adeptj.runtime.common.Verb;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.core.ContainerInitializer;
import com.adeptj.runtime.logging.LogbackInitializer;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.servlet.AuthServlet;
import com.adeptj.runtime.servlet.ErrorPageServlet;
import com.adeptj.runtime.servlet.ToolsServlet;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.Version;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.RequestBufferingHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.servlet.Servlets;
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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import static com.adeptj.runtime.common.Constants.REGEX_EQ;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.SYS_PROP_SERVER_PORT;
import static com.adeptj.runtime.common.Constants.TOOLS_DASHBOARD_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_LOGIN_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_LOGOUT_URI;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.ATTRIBUTE_NAME;
import static java.util.stream.Collectors.toMap;
import static javax.servlet.http.HttpServletRequest.FORM_AUTH;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

/**
 * Provisions the Undertow Web Server, start OSGi framework and much more.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final String KEY_IGNORE_FLUSH = "common.ignore-flush";

    private static final int SYS_TASK_THREAD_MULTIPLIER = 2;

    private static final int WORKER_TASK_THREAD_MULTIPLIER = 8;

    private static final String KEY_WORKER_TASK_MAX_THREADS = "worker-task-max-threads";

    private static final String KEY_WORKER_TASK_CORE_THREADS = "worker-task-core-threads";

    private static final String KEY_WORKER_OPTIONS = "worker-options";

    private static final String KEY_KEYSTORE = "keyStore";

    private static final String KEY_KEYPWD = "keyPwd";

    private static final String KEY_KEYSTORE_PWD = "keyStorePwd";

    private static final String KEY_HTTPS = "https";

    private static final String SYS_PROP_ENABLE_HTTP2 = "enable.http2";

    private static final String KEY_AJP = "ajp";

    private static final String SYS_PROP_ENABLE_AJP = "enable.ajp";

    private static final String SYS_PROP_CHECK_PORT = "adeptj.rt.port.check";

    private static final String KEY_DEFAULT_ENCODING = "common.default-encoding";

    private static final String REALM = "AdeptJ Realm";

    private static final String SYS_PROP_SESSION_TIMEOUT = "adeptj.session.timeout";

    private static final String SYS_PROP_ENABLE_REQ_BUFF = "enable.req.buffering";

    private static final String SYS_PROP_REQ_BUFF_MAX_BUFFERS = "req.buff.maxBuffers";

    private static final String SYS_PROP_MAX_CONCUR_REQ = "max.concurrent.requests";

    private static final String KEY_SESSION_TIMEOUT = "common.session-timeout";

    private static final String KEY_HTTP_ONLY = "common.session-cookie-httpOnly";

    private static final String KEY_CHANGE_SESSIONID_ON_LOGIN = "common.change-sessionId-on-login";

    private static final String KEY_INVALIDATE_SESSION_ON_LOGOUT = "common.invalidate-session-on-logout";

    private static final String KEY_USE_CACHED_AUTH_MECHANISM = "common.use-cached-auth-mechanism";

    private static final String KEY_WS_IO_THREADS = "io-threads";

    private static final String KEY_WS_TASK_CORE_THREADS = "task-core-threads";

    private static final String KEY_WS_TASK_MAX_THREADS = "task-max-threads";

    private static final String KEY_WS_TCP_NO_DELAY = "tcp-no-delay";

    private static final String KEY_WS_WEB_SOCKET_OPTIONS = "webSocket-options";

    private static final String KEY_WS_USE_DIRECT_BUFFER = "use-direct-buffer";

    private static final String KEY_WS_BUFFER_SIZE = "buffer-size";

    private static final String KEY_MULTIPART_FILE_LOCATION = "common.multipart-file-location";

    private static final String KEY_MULTIPART_MAX_FILE_SIZE = "common.multipart-max-file-size";

    private static final String KEY_MULTIPART_MAX_REQUEST_SIZE = "common.multipart-max-request-size";

    private static final String KEY_MULTIPART_FILE_SIZE_THRESHOLD = "common.multipart-file-size-threshold";

    private static final String KEY_AUTH_ROLES = "common.auth-roles";

    private static final String KEY_SECURED_URLS_ALLOWED_METHODS = "common.secured-urls-allowed-methods";

    private static final String KEY_SECURED_URLS = "common.secured-urls";

    private static final String KEY_ERROR_PAGES = "error-pages";

    private static final String ERROR_PAGE_SERVLET = "AdeptJ ErrorPageServlet";

    private static final String TOOLS_ERROR_URL = "/tools/error/*";

    private static final String TOOLS_SERVLET = "AdeptJ ToolsServlet";

    private static final String TOOLS_DASHBOARD_URL = "/tools/dashboard";

    private static final String AUTH_SERVLET = "AdeptJ AuthServlet";

    private static final int IDX_ZERO = 0;

    private static final int IDX_ONE = 1;

    // No instantiation.
    private Server() {
    }

    public static void start(String[] args) throws ServletException {
        Map<String, String> commands = Arrays.stream(args)
                .map(cmd -> cmd.split(REGEX_EQ))
                .collect(toMap(cmdArray -> cmdArray[IDX_ZERO], cmdArray -> cmdArray[IDX_ONE]));
        Config undertowConf = Configs.DEFAULT.undertow();
        Config httpConf = undertowConf.getConfig(KEY_HTTP);
        LOGGER.debug("Commands to AdeptJ Runtime: {}", commands);
        int httpPort = handlePortAvailability(httpConf);
        LOGGER.info("Starting AdeptJ Runtime @port: [{}]", httpPort);
        printBanner();
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo(undertowConf));
        manager.deploy();
        Builder builder = Undertow.builder();
        optimizeWorkerOptions(builder, undertowConf);
        ServerOptions.build(builder, undertowConf);
        builder.addHttpListener(httpPort, httpConf.getString(KEY_HOST));
        enableHttp2(undertowConf, builder);
        enableAJP(undertowConf, builder);
        GracefulShutdownHandler rootHandler = rootHandler(headersHandler(manager.start(), undertowConf), undertowConf);
        Undertow server = builder.setHandler(rootHandler).build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(server, manager, rootHandler));
        launchBrowser(commands, httpPort);
        if (!Environment.isServerConfFileExists()) {
            createServerConfFile();
        }
    }

    private static void printBanner() {
        try (InputStream stream = Server.class.getResourceAsStream(BANNER_TXT)) {
            LOGGER.info(IOUtils.toString(stream)); // NOSONAR
        } catch (IOException ex) {
            // Just log it, its not critical.
            LOGGER.error("IOException!!", ex);
        }
    }

    private static void launchBrowser(Map<String, String> arguments, int httpPort) {
        if (Boolean.parseBoolean(arguments.get(ARG_OPEN_CONSOLE))) {
            try {
                Environment.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL, httpPort)));
            } catch (IOException ex) {
                // Just log it, its okay if browser is not launched.
                LOGGER.error("IOException!!", ex);
            }
        }
    }

    private static void createServerConfFile() {
        try (InputStream stream = Server.class.getResourceAsStream("/reference.conf")) {
            Files.write(Paths.get(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, SERVER_CONF_FILE),
                    IOUtils.toBytes(stream), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            LOGGER.error("IOException!!", ex);
        }
    }

    private static void optimizeWorkerOptions(Builder builder, Config undertowConf) {
        if (Environment.isProd()) {
            Config workerOptions = undertowConf.getConfig(KEY_WORKER_OPTIONS);
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
    }

    private static void enableHttp2(Config undertowConf, Builder undertowBuilder) {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_HTTP2)) {
            Config httpsConf = undertowConf.getConfig(KEY_HTTPS);
            int httpsPort = httpsConf.getInt(KEY_PORT);
            if (!Environment.useProvidedKeyStore()) {
                System.setProperty("javax.net.ssl.keyStore", httpsConf.getString(KEY_KEYSTORE));
                System.setProperty("javax.net.ssl.keyStorePassword", httpsConf.getString(KEY_KEYSTORE_PWD));
                System.setProperty("javax.net.ssl.keyPassword", httpsConf.getString(KEY_KEYPWD));
                LOGGER.info("HTTP2 will be enabled @ port: [{}] using bundled KeyStore.", httpsPort);
            }
            undertowBuilder.addHttpsListener(httpsPort, httpsConf.getString(KEY_HOST), SslContextFactory.createSslContext());
        }
    }

    private static void enableAJP(Config undertowConf, Builder undertowBuilder) {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_AJP)) {
            Config ajpConf = undertowConf.getConfig(KEY_AJP);
            int ajpPort = ajpConf.getInt(KEY_PORT);
            undertowBuilder.addAjpListener(ajpPort, ajpConf.getString(KEY_HOST));
            LOGGER.info("AJP enabled @ port: [{}]", ajpPort);
        }
    }

    private static int handlePortAvailability(Config httpConf) {
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
            LogbackInitializer.stopLogback();
            System.exit(-1); // NOSONAR
        }
        return port;
    }

    private static boolean isPortAvailable(int port) {
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

    private static RequestBufferingHandler reqBufferingHandler(HttpHandler initialHandler, Config cfg) {
        return new RequestBufferingHandler(initialHandler,
                Integer.getInteger(SYS_PROP_REQ_BUFF_MAX_BUFFERS, cfg.getInt(KEY_REQ_BUFF_MAX_BUFFERS)));
    }

    private static SetHeadersHandler headersHandler(HttpHandler servletInitialHandler, Config cfg) {
        Map<HttpString, String> headers = new HashMap<>();
        headers.put(HttpString.tryFromString(HEADER_SERVER), cfg.getString(KEY_HEADER_SERVER));
        if (!Environment.isProd()) {
            headers.put(HttpString.tryFromString(HEADER_X_POWERED_BY), Version.getFullVersionString());
        }
        return Boolean.getBoolean(SYS_PROP_ENABLE_REQ_BUFF) ?
                new SetHeadersHandler(reqBufferingHandler(servletInitialHandler, cfg), headers) :
                new SetHeadersHandler(servletInitialHandler, headers);
    }

    private static PredicateHandler predicateHandler(HttpHandler headersHandler) {
        return Handlers.predicate(exchange -> CONTEXT_PATH.equals(exchange.getRequestURI()),
                Handlers.redirect(TOOLS_DASHBOARD_URI), headersHandler);
    }

    private static Set<HttpString> allowedMethods(Config cfg) {
        return cfg.getStringList(KEY_ALLOWED_METHODS)
                .stream()
                .map(Verb::from)
                .collect(Collectors.toSet());
    }

    private static GracefulShutdownHandler rootHandler(HttpHandler headersHandler, Config cfg) {
        return Handlers.gracefulShutdown(
                new RequestLimitingHandler(Integer.getInteger(SYS_PROP_MAX_CONCUR_REQ, cfg.getInt(KEY_MAX_CONCURRENT_REQS)),
                        new AllowedMethodsHandler(predicateHandler(headersHandler), allowedMethods(cfg))));
    }

    private static List<ErrorPage> errorPages(Config cfg) {
        return cfg.getObject(KEY_ERROR_PAGES).unwrapped()
                .entrySet()
                .stream()
                .map(e -> Servlets.errorPage(String.valueOf(e.getValue()), Integer.parseInt(e.getKey())))
                .collect(Collectors.toList());
    }

    private static ServletContainerInitializerInfo sciInfo() {
        Set<Class<?>> handlesTypes = new HashSet<>();
        handlesTypes.add(FrameworkLauncher.class);
        handlesTypes.add(DefaultStartupAware.class);
        return new ServletContainerInitializerInfo(ContainerInitializer.class, handlesTypes);
    }

    private static SecurityConstraint securityConstraint(Config cfg) {
        return Servlets.securityConstraint()
                .addRolesAllowed(cfg.getStringList(KEY_AUTH_ROLES))
                .addWebResourceCollection(Servlets.webResourceCollection()
                        .addHttpMethods(cfg.getStringList(KEY_SECURED_URLS_ALLOWED_METHODS))
                        .addUrlPatterns(cfg.getStringList(KEY_SECURED_URLS)));
    }

    private static List<ServletInfo> servlets() {
        List<ServletInfo> servlets = new ArrayList<>();
        servlets.add(Servlets.servlet(ERROR_PAGE_SERVLET, ErrorPageServlet.class).addMapping(TOOLS_ERROR_URL).setAsyncSupported(true));
        servlets.add(Servlets.servlet(TOOLS_SERVLET, ToolsServlet.class).addMapping(TOOLS_DASHBOARD_URL).setAsyncSupported(true));
        servlets.add(Servlets.servlet(AUTH_SERVLET, AuthServlet.class).addMappings(TOOLS_LOGIN_URI, TOOLS_LOGOUT_URI).setAsyncSupported(true));
        return servlets;
    }

    private static MultipartConfigElement defaultMultipartConfig(Config cfg) {
        return Servlets.multipartConfig(cfg.getString(KEY_MULTIPART_FILE_LOCATION),
                cfg.getLong(KEY_MULTIPART_MAX_FILE_SIZE),
                cfg.getLong(KEY_MULTIPART_MAX_REQUEST_SIZE),
                cfg.getInt(KEY_MULTIPART_FILE_SIZE_THRESHOLD));
    }

    private static WebSocketDeploymentInfo webSocketDeploymentInfo(Config cfg) {
        Config wsOptions = cfg.getConfig(KEY_WS_WEB_SOCKET_OPTIONS);
        return new WebSocketDeploymentInfo()
                .setWorker(webSocketWorker(wsOptions))
                .setBuffers(new DefaultByteBufferPool(wsOptions.getBoolean(KEY_WS_USE_DIRECT_BUFFER), wsOptions.getInt(KEY_WS_BUFFER_SIZE)))
                .addEndpoint(ServerLogsWebSocket.class);
    }

    private static XnioWorker webSocketWorker(Config wsOptions) {
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

    private static int sessionTimeout(Config cfg) {
        return Integer.getInteger(SYS_PROP_SESSION_TIMEOUT, cfg.getInt(KEY_SESSION_TIMEOUT));
    }

    private static ServletSessionConfig sessionConfig(Config cfg) {
        return new ServletSessionConfig().setHttpOnly(cfg.getBoolean(KEY_HTTP_ONLY));
    }

    private static DeploymentInfo deploymentInfo(Config cfg) {
        return Servlets.deployment()
                .setDeploymentName(DEPLOYMENT_NAME)
                .setContextPath(CONTEXT_PATH)
                .setClassLoader(Server.class.getClassLoader())
                .setIgnoreFlush(cfg.getBoolean(KEY_IGNORE_FLUSH))
                .setDefaultEncoding(cfg.getString(KEY_DEFAULT_ENCODING))
                .setDefaultSessionTimeout(sessionTimeout(cfg))
                .setChangeSessionIdOnLogin(cfg.getBoolean(KEY_CHANGE_SESSIONID_ON_LOGIN))
                .setInvalidateSessionOnLogout(cfg.getBoolean(KEY_INVALIDATE_SESSION_ON_LOGOUT))
                .setIdentityManager(new SimpleIdentityManager(cfg))
                .setUseCachedAuthenticationMechanism(cfg.getBoolean(KEY_USE_CACHED_AUTH_MECHANISM))
                .setLoginConfig(Servlets.loginConfig(FORM_AUTH, REALM, TOOLS_LOGIN_URI, TOOLS_LOGIN_URI))
                .addServletContainerInitalizer(sciInfo())
                .addSecurityConstraint(securityConstraint(cfg))
                .addServlets(servlets())
                .addErrorPages(errorPages(cfg))
                .setDefaultMultipartConfig(defaultMultipartConfig(cfg))
                .addInitialHandlerChainWrapper(new ServletInitialHandlerWrapper())
                .addServletContextAttribute(ATTRIBUTE_NAME, webSocketDeploymentInfo(cfg))
                .setServletSessionConfig(sessionConfig(cfg));
    }
}