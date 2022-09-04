package com.adeptj.runtime.undertow;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.Constants;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.adeptj.runtime.kernel.util.Environment;
import com.adeptj.runtime.kernel.util.Times;
import com.adeptj.runtime.undertow.core.ServerOptions;
import com.adeptj.runtime.undertow.core.SimpleIdentityManager;
import com.adeptj.runtime.undertow.core.SocketOptions;
import com.adeptj.runtime.undertow.core.SslContextFactory;
import com.adeptj.runtime.undertow.core.WorkerOptions;
import com.adeptj.runtime.undertow.handler.HealthCheckHandler;
import com.adeptj.runtime.undertow.handler.ServletInitialHandlerWrapper;
import com.adeptj.runtime.undertow.predicate.ContextPathPredicate;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.RequestBufferingHandler;
import io.undertow.server.handlers.RequestLimit;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.server.handlers.SetHeaderHandler;
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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.adeptj.runtime.kernel.Constants.ADMIN_LOGIN_URI;
import static com.adeptj.runtime.kernel.Constants.DEPLOYMENT_NAME;
import static com.adeptj.runtime.kernel.Constants.KEY_ALLOWED_METHODS;
import static com.adeptj.runtime.kernel.Constants.KEY_ERROR_HANDLER_CODES;
import static com.adeptj.runtime.kernel.Constants.KEY_ERROR_HANDLER_PATH;
import static com.adeptj.runtime.kernel.Constants.KEY_HEADER_SERVER;
import static com.adeptj.runtime.kernel.Constants.KEY_HOST;
import static com.adeptj.runtime.kernel.Constants.KEY_MAX_CONCURRENT_REQUESTS;
import static com.adeptj.runtime.kernel.Constants.KEY_PORT;
import static com.adeptj.runtime.kernel.Constants.KEY_REQ_BUFF_MAX_BUFFERS;
import static com.adeptj.runtime.kernel.Constants.KEY_REQ_LIMIT_QUEUE_SIZE;
import static com.adeptj.runtime.kernel.Constants.KEY_SYSTEM_CONSOLE_PATH;
import static com.adeptj.runtime.kernel.Constants.SERVER_CONF_CP_RESOURCE;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_OVERWRITE_SERVER_CONF;
import static com.adeptj.runtime.undertow.core.ServerConstants.DEFAULT_WAIT_TIME;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_AUTH_ROLES;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_CHANGE_SESSION_ID_ON_LOGIN;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_CONTEXT_PATH;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_DEFAULT_ENCODING;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_HEALTH_CHECK_HANDLER_PATH;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_HTTPS;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_HTTP_ONLY;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_IGNORE_FLUSH;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_INVALIDATE_SESSION_ON_LOGOUT;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_MULTIPART_FILE_LOCATION;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_MULTIPART_FILE_SIZE_THRESHOLD;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_MULTIPART_MAX_FILE_SIZE;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_MULTIPART_MAX_REQUEST_SIZE;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_PROTECTED_PATHS;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_PROTECTED_PATHS_SECURED_FOR_METHODS;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_SESSION_TIMEOUT;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_USE_CACHED_AUTH_MECHANISM;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_WORKER_OPTIONS;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_WORKER_TASK_CORE_THREADS;
import static com.adeptj.runtime.undertow.core.ServerConstants.KEY_WORKER_TASK_MAX_THREADS;
import static com.adeptj.runtime.undertow.core.ServerConstants.REALM;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_ENABLE_HTTP2;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_ENABLE_REQ_BUFF;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_MAX_CONCUR_REQ;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_REQ_BUFF_MAX_BUFFERS;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_REQ_LIMIT_QUEUE_SIZE;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_SERVER_HTTPS_PORT;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_SESSION_TIMEOUT;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_SHUTDOWN_WAIT_TIME;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_SYS_TASK_THREAD_MULTIPLIER;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_PROP_WORKER_TASK_THREAD_MULTIPLIER;
import static com.adeptj.runtime.undertow.core.ServerConstants.SYS_TASK_THREAD_MULTIPLIER;
import static com.adeptj.runtime.undertow.core.ServerConstants.WORKER_TASK_THREAD_MULTIPLIER;
import static io.undertow.util.Headers.SERVER_STRING;
import static javax.servlet.http.HttpServletRequest.FORM_AUTH;
import static org.xnio.Options.WORKER_TASK_CORE_THREADS;
import static org.xnio.Options.WORKER_TASK_MAX_THREADS;

public class UndertowServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UndertowServer.class);

    private Undertow undertow;

    private DeploymentManager deploymentManager;

    private GracefulShutdownHandler rootHandler;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.UNDERTOW;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
        Config undertowConf = ConfigProvider.getInstance().getServerConfig(this.getRuntime());
        Config httpConf = undertowConf.getConfig(Constants.KEY_HTTP);
        int port = this.resolvePort(httpConf);
        LOGGER.info("Starting AdeptJ Runtime @port: [{}]", port);
        try {
            this.deploymentManager = Servlets.defaultContainer().addDeployment(this.deploymentInfo(undertowConf, deployment));
            this.deploymentManager.deploy();
            // this.createOrUpdateServerConfFile();
            HttpHandler httpContinueReadHandler = this.deploymentManager.start();
            this.rootHandler = this.createHandlerChain(httpContinueReadHandler, undertowConf);
            Undertow.Builder undertowBuilder = Undertow.builder();
            this.setWorkerOptions(undertowBuilder, undertowConf);
            new SocketOptions().setOptions(undertowBuilder, undertowConf);
            new ServerOptions().setOptions(undertowBuilder, undertowConf);
            this.undertow = this.addHttpsListener(undertowBuilder, undertowConf)
                    .addHttpListener(port, httpConf.getString(KEY_HOST))
                    .setHandler(this.rootHandler)
                    .build();
            this.undertow.start();
        } catch (Exception ex) { // NOSONAR
            throw new RuntimeInitializationException(ex);
        }
    }

    @Override
    public void stop() {
        long startTime = System.nanoTime();
        LOGGER.info("Stopping AdeptJ Runtime!!");
        try {
            this.gracefulShutdown();
            // Calls stop on LifecycleObjects - io.undertow.servlet.core.Lifecycle
            this.deploymentManager.stop();
            // Calls contextDestroyed on all registered ServletContextListener and performs other cleanup tasks.
            this.deploymentManager.undeploy();
            this.undertow.stop();
            LOGGER.info("AdeptJ Runtime stopped in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable ex) { // NOSONAR
            LOGGER.error("Exception while stopping AdeptJ Runtime!!", ex);
        } finally {
            // SLF4JBridgeHandler.uninstall();
            // Let the Logback cleans up it's state.
            // LogbackManagerHolder.getInstance().getLogbackManager().stopLogback();
        }
    }

    @Override
    protected void doRegisterServlet(com.adeptj.runtime.kernel.ServletInfo info) {
        // NOP, as it is done at the DeploymentInfo creation time.
    }

    @Override
    protected void doRegisterFilter(FilterInfo info) {

    }

    @Override
    public void registerErrorPages(List<Integer> errorCodes) {

    }

    @Override
    public void addServletContextAttribute(String name, Object value) {

    }

    private void gracefulShutdown() {
        try {
            this.rootHandler.shutdown();
            this.rootHandler.awaitShutdown(Long.getLong(SYS_PROP_SHUTDOWN_WAIT_TIME, DEFAULT_WAIT_TIME));
        } catch (InterruptedException ie) {
            LOGGER.error("Error while waiting for GracefulShutdownHandler to shutdown!!", ie);
            // SONAR - "InterruptedException" should not be ignored
            // Can't really rethrow it as we are yet to stop the server and anyway it's a shutdown hook
            // and JVM itself will be shutting down shortly.
            Thread.currentThread().interrupt();
        }
    }

    private void createOrUpdateServerConfFile() {
        Path confPath = Environment.getServerConfPath();
        if (confPath.toFile().exists()) {
            if (Boolean.getBoolean(SYS_PROP_OVERWRITE_SERVER_CONF)) {
                this.doCreateOrUpdateServerConfFile(confPath);
            }
        } else {
            this.doCreateOrUpdateServerConfFile(confPath);
        }
    }

    private void doCreateOrUpdateServerConfFile(Path confFile) {
        try (InputStream stream = this.getClass().getResourceAsStream(SERVER_CONF_CP_RESOURCE)) {
            if (stream != null) {
                Files.write(confFile, IOUtils.toByteArray(stream));
            }
        } catch (IOException ex) {
            LOGGER.error("Exception while creating server conf file!!", ex);
        }
    }

    private void setWorkerOptions(Undertow.Builder builder, Config undertowConf) {
        long startTime = System.nanoTime();
        // Note : For a 16 core system, number of worker task core and max threads will be.
        // 1. core task thread: 128 (16[cores] * 8)
        // 2. max task thread: 128 * 2 = 256
        // Default settings would have set the following.
        // 1. core task thread: 128 (16[cores] * 8)
        // 2. max task thread: 128 (Same as core task thread)
        Config config = undertowConf.getConfig(KEY_WORKER_OPTIONS);
        // defaults to 64
        int cfgCoreTaskThreads = config.getInt(KEY_WORKER_TASK_CORE_THREADS);
        LOGGER.info("Configured worker task core threads: [{}]", cfgCoreTaskThreads);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LOGGER.info("No. of CPU available: [{}]", availableProcessors);
        int calcCoreTaskThreads = availableProcessors *
                Integer.getInteger(SYS_PROP_WORKER_TASK_THREAD_MULTIPLIER, WORKER_TASK_THREAD_MULTIPLIER);
        LOGGER.info("Calculated worker task core threads: [{}]", calcCoreTaskThreads);
        // defaults to double of [worker-task-core-threads] i.e 128
        int cfgMaxTaskThreads = config.getInt(KEY_WORKER_TASK_MAX_THREADS);
        LOGGER.info("Configured worker task max threads: [{}]", cfgMaxTaskThreads);
        int calcMaxTaskThreads = calcCoreTaskThreads *
                Integer.getInteger(SYS_PROP_SYS_TASK_THREAD_MULTIPLIER, SYS_TASK_THREAD_MULTIPLIER);
        LOGGER.info("Calculated worker task max threads: [{}]", calcMaxTaskThreads);
        WorkerOptions options = new WorkerOptions();
        options.setOptions(builder, undertowConf);
        options.overrideOption(builder, WORKER_TASK_CORE_THREADS, Math.max(cfgCoreTaskThreads, calcCoreTaskThreads))
                .overrideOption(builder, WORKER_TASK_MAX_THREADS, Math.max(cfgMaxTaskThreads, calcMaxTaskThreads));
        LOGGER.info("Undertow WorkerOptions configured in [{}] ms!!", Times.elapsedMillis(startTime));
    }

    private Undertow.Builder addHttpsListener(Undertow.Builder builder, Config undertowConf) throws GeneralSecurityException {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_HTTP2)) {
            Config httpsConf = undertowConf.getConfig(KEY_HTTPS);
            int httpsPort = Integer.getInteger(SYS_PROP_SERVER_HTTPS_PORT, httpsConf.getInt(KEY_PORT));
            SSLContext sslContext = SslContextFactory.newSslContext(httpsConf);
            builder.addHttpsListener(httpsPort, httpsConf.getString(KEY_HOST), sslContext);
            LOGGER.info("HTTP2 enabled @ port: {}.", httpsPort);
        }
        return builder;
    }

    /**
     * Chaining of Undertow {@link HttpHandler} instances as follows.
     * <p>
     * 1. GracefulShutdownHandler
     * 2. RequestLimitingHandler
     * 3. AllowedMethodsHandler
     * 4. PathHandler which resolves to either PredicateHandler or HealthCheckHandler
     * 5. RequestBufferingHandler if request buffering is enabled, wrapped in SetHeadersHandler
     * 5. And Finally HttpContinueReadHandler
     *
     * @param httpContinueReadHandler the {@link io.undertow.server.handlers.HttpContinueReadHandler}
     * @param cfg                     the undertow server config object.
     * @return GracefulShutdownHandler as the root handler
     */
    private GracefulShutdownHandler createHandlerChain(HttpHandler httpContinueReadHandler, Config cfg) {
        RedirectHandler contextHandler = Handlers.redirect(cfg.getString(KEY_SYSTEM_CONSOLE_PATH));
        RequestBufferingHandler requestBufferingHandler = new RequestBufferingHandler(httpContinueReadHandler,
                Integer.getInteger(SYS_PROP_REQ_BUFF_MAX_BUFFERS, cfg.getInt(KEY_REQ_BUFF_MAX_BUFFERS)));
        HttpHandler headersHandler = Boolean.getBoolean(SYS_PROP_ENABLE_REQ_BUFF) ?
                new SetHeaderHandler(requestBufferingHandler, SERVER_STRING, cfg.getString(KEY_HEADER_SERVER)) :
                new SetHeaderHandler(httpContinueReadHandler, SERVER_STRING, cfg.getString(KEY_HEADER_SERVER));
        ContextPathPredicate contextPathPredicate = new ContextPathPredicate(cfg.getString(KEY_CONTEXT_PATH));
        PredicateHandler predicateHandler = Handlers.predicate(contextPathPredicate, contextHandler, headersHandler);
        PathHandler pathHandler = Handlers.path(predicateHandler)
                .addPrefixPath(cfg.getString(KEY_HEALTH_CHECK_HANDLER_PATH), new HealthCheckHandler());
        AllowedMethodsHandler allowedMethodsHandler = new AllowedMethodsHandler(pathHandler, this.allowedMethods(cfg));
        int maxConcurrentRequests = Integer.getInteger(SYS_PROP_MAX_CONCUR_REQ, cfg.getInt(KEY_MAX_CONCURRENT_REQUESTS));
        int queueSize = Integer.getInteger(SYS_PROP_REQ_LIMIT_QUEUE_SIZE, cfg.getInt(KEY_REQ_LIMIT_QUEUE_SIZE));
        RequestLimit limit = new RequestLimit(maxConcurrentRequests, queueSize);
        RequestLimitingHandler requestLimitingHandler = Handlers.requestLimitingHandler(limit, allowedMethodsHandler);
        return Handlers.gracefulShutdown(requestLimitingHandler);
    }

    private Set<HttpString> allowedMethods(Config cfg) {
        return cfg.getStringList(KEY_ALLOWED_METHODS)
                .stream()
                .map(HttpString::tryFromString)
                .collect(Collectors.toSet());
    }

    private List<ErrorPage> errorPages(Config undertowConf) {
        return undertowConf.getIntList(KEY_ERROR_HANDLER_CODES)
                .stream()
                .map(code -> Servlets.errorPage(undertowConf.getString(KEY_ERROR_HANDLER_PATH), code))
                .collect(Collectors.toList());
    }

    private ServletContainerInitializerInfo sciInfo(SciInfo sciInfo) {
        return new ServletContainerInitializerInfo(sciInfo.getSciClass(), sciInfo.getHandleTypes());
    }

    private SecurityConstraint securityConstraint(Config cfg) {
        return Servlets.securityConstraint()
                .addRolesAllowed(cfg.getStringList(KEY_AUTH_ROLES))
                .addWebResourceCollection(Servlets.webResourceCollection()
                        .addUrlPatterns(cfg.getStringList(KEY_PROTECTED_PATHS))
                        .addHttpMethods(cfg.getStringList(KEY_PROTECTED_PATHS_SECURED_FOR_METHODS)));
    }

    private List<ServletInfo> servlets(ServletDeployment deployment) {
        List<ServletInfo> servlets = new ArrayList<>();
        for (com.adeptj.runtime.kernel.ServletInfo info : deployment.getServletInfos()) {
            servlets.add(Servlets.servlet(info.getServletName(), info.getServletClass())
                    .addMapping(info.getPath())
                    .setAsyncSupported(true));
        }
        return servlets;
    }

    private MultipartConfigElement defaultMultipartConfig(Config cfg) {
        return Servlets.multipartConfig(cfg.getString(KEY_MULTIPART_FILE_LOCATION),
                cfg.getLong(KEY_MULTIPART_MAX_FILE_SIZE),
                cfg.getLong(KEY_MULTIPART_MAX_REQUEST_SIZE),
                cfg.getInt(KEY_MULTIPART_FILE_SIZE_THRESHOLD));
    }

    private int sessionTimeout(Config cfg) {
        return Integer.getInteger(SYS_PROP_SESSION_TIMEOUT, cfg.getInt(KEY_SESSION_TIMEOUT));
    }

    private ServletSessionConfig sessionConfig(Config cfg) {
        return new ServletSessionConfig().setHttpOnly(cfg.getBoolean(KEY_HTTP_ONLY));
    }

    private DeploymentInfo deploymentInfo(Config cfg, ServletDeployment deployment) {
        return Servlets.deployment()
                .setDeploymentName(DEPLOYMENT_NAME)
                .setContextPath(cfg.getString(KEY_CONTEXT_PATH))
                .setClassLoader(this.getClass().getClassLoader())
                .addServletContainerInitializer(this.sciInfo(deployment.getSciInfo()))
                .setIgnoreFlush(cfg.getBoolean(KEY_IGNORE_FLUSH))
                .setDefaultEncoding(cfg.getString(KEY_DEFAULT_ENCODING))
                .setDefaultSessionTimeout(this.sessionTimeout(cfg))
                .setChangeSessionIdOnLogin(cfg.getBoolean(KEY_CHANGE_SESSION_ID_ON_LOGIN))
                .setInvalidateSessionOnLogout(cfg.getBoolean(KEY_INVALIDATE_SESSION_ON_LOGOUT))
                .setIdentityManager(new SimpleIdentityManager(this.getUserManager(), cfg))
                .setUseCachedAuthenticationMechanism(cfg.getBoolean(KEY_USE_CACHED_AUTH_MECHANISM))
                .setLoginConfig(Servlets.loginConfig(FORM_AUTH, REALM, ADMIN_LOGIN_URI, ADMIN_LOGIN_URI))
                .addSecurityConstraint(this.securityConstraint(cfg))
                .addServlets(this.servlets(deployment))
                .addErrorPages(this.errorPages(cfg))
                .setDefaultMultipartConfig(this.defaultMultipartConfig(cfg))
                .addInitialHandlerChainWrapper(new ServletInitialHandlerWrapper())
                .setServletSessionConfig(this.sessionConfig(cfg))
                .setCrawlerSessionManagerConfig(new CrawlerSessionManagerConfig());
    }
}
