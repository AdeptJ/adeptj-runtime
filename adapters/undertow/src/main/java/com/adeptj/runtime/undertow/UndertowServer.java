package com.adeptj.runtime.undertow;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.Constants;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.adeptj.runtime.kernel.util.SslContextFactory;
import com.adeptj.runtime.kernel.util.Times;
import com.adeptj.runtime.undertow.core.ServerOptions;
import com.adeptj.runtime.undertow.core.SimpleIdentityManager;
import com.adeptj.runtime.undertow.core.SocketOptions;
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
import jakarta.servlet.MultipartConfigElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
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
import static com.adeptj.runtime.kernel.Constants.KEY_HOST;
import static com.adeptj.runtime.kernel.Constants.KEY_MAX_CONCURRENT_REQUESTS;
import static com.adeptj.runtime.kernel.Constants.KEY_PORT;
import static com.adeptj.runtime.kernel.Constants.KEY_REQ_BUFF_MAX_BUFFERS;
import static com.adeptj.runtime.kernel.Constants.KEY_REQ_LIMIT_QUEUE_SIZE;
import static com.adeptj.runtime.kernel.Constants.KEY_SYSTEM_CONSOLE_PATH;
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
import static jakarta.servlet.http.HttpServletRequest.FORM_AUTH;
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
        ConfigProvider configProvider = ConfigProvider.getInstance();
        Config appConfig = configProvider.getApplicationConfig();
        Config mainConfig = configProvider.getMainConfig(appConfig);
        Config undertowConfig = configProvider.getServerConfig(this.getRuntime(), appConfig);
        int port = this.resolvePort(appConfig);
        try {
            DeploymentInfo deploymentInfo = this.deploymentInfo(mainConfig, undertowConfig, deployment);
            this.deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
            this.deploymentManager.deploy();
            HttpHandler httpContinueReadHandler = this.deploymentManager.start();
            this.rootHandler = this.createHandlerChain(httpContinueReadHandler, mainConfig, undertowConfig);
            Undertow.Builder undertowBuilder = Undertow.builder();
            this.setWorkerOptions(undertowBuilder, undertowConfig);
            new SocketOptions().setOptions(undertowBuilder, undertowConfig);
            new ServerOptions().setOptions(undertowBuilder, undertowConfig);
            this.undertow = this.addHttpsListener(undertowBuilder, undertowConfig)
                    .addHttpListener(port, undertowConfig.getConfig(Constants.KEY_HTTP).getString(KEY_HOST))
                    .setHandler(this.rootHandler)
                    .build();
            this.undertow.start();
        } catch (Exception ex) { // NOSONAR
            throw new RuntimeInitializationException(ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doStop() throws Exception {
        this.gracefulShutdown();
        // Calls stop on LifecycleObjects - io.undertow.servlet.core.Lifecycle
        this.deploymentManager.stop();
        // Calls contextDestroyed on all registered ServletContextListener and performs other cleanup tasks.
        this.deploymentManager.undeploy();
        this.undertow.stop();
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
        this.deploymentManager.getDeployment().getServletContext().setAttribute(name, value);
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

    private void setWorkerOptions(Undertow.Builder builder, Config undertowConfig) {
        long startTime = System.nanoTime();
        // Note : For a 16 core system, number of worker task core and max threads will be.
        // 1. core task thread: 128 (16[cores] * 8)
        // 2. max task thread: 128 * 2 = 256
        // Default settings would have set the following.
        // 1. core task thread: 128 (16[cores] * 8)
        // 2. max task thread: 128 (Same as core task thread)
        Config config = undertowConfig.getConfig(KEY_WORKER_OPTIONS);
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
        options.setOptions(builder, undertowConfig);
        options.overrideOption(builder, WORKER_TASK_CORE_THREADS, Math.max(cfgCoreTaskThreads, calcCoreTaskThreads))
                .overrideOption(builder, WORKER_TASK_MAX_THREADS, Math.max(cfgMaxTaskThreads, calcMaxTaskThreads));
        LOGGER.info("Undertow WorkerOptions configured in [{}] ms!!", Times.elapsedMillis(startTime));
    }

    private Undertow.Builder addHttpsListener(Undertow.Builder builder, Config undertowConfig) throws GeneralSecurityException {
        if (Boolean.getBoolean(SYS_PROP_ENABLE_HTTP2)) {
            Config httpsConf = undertowConfig.getConfig(KEY_HTTPS);
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
     * @param mainConfig              the main config object in application.conf.
     * @param undertowConfig          the undertow server config object in reference.conf.
     * @return GracefulShutdownHandler as the root handler
     */
    private GracefulShutdownHandler createHandlerChain(HttpHandler httpContinueReadHandler, Config mainConfig,
                                                       Config undertowConfig) {
        RedirectHandler contextHandler = Handlers.redirect(mainConfig.getString(KEY_SYSTEM_CONSOLE_PATH));
        HttpHandler requestBufferingHandler = null;
        if (Boolean.getBoolean(SYS_PROP_ENABLE_REQ_BUFF)) {
            requestBufferingHandler = new RequestBufferingHandler(httpContinueReadHandler,
                    Integer.getInteger(SYS_PROP_REQ_BUFF_MAX_BUFFERS, undertowConfig.getInt(KEY_REQ_BUFF_MAX_BUFFERS)));
        }
        ContextPathPredicate contextPathPredicate = new ContextPathPredicate(mainConfig.getString(KEY_CONTEXT_PATH));
        PredicateHandler predicateHandler = Handlers.predicate(contextPathPredicate, contextHandler,
                (requestBufferingHandler == null ? httpContinueReadHandler : requestBufferingHandler));
        PathHandler pathHandler = Handlers.path(predicateHandler)
                .addPrefixPath(mainConfig.getString(KEY_HEALTH_CHECK_HANDLER_PATH), new HealthCheckHandler());
        AllowedMethodsHandler allowedMethodsHandler = new AllowedMethodsHandler(pathHandler, this.allowedMethods(mainConfig));
        int maxConcurrentRequests = Integer.getInteger(SYS_PROP_MAX_CONCUR_REQ, undertowConfig.getInt(KEY_MAX_CONCURRENT_REQUESTS));
        int queueSize = Integer.getInteger(SYS_PROP_REQ_LIMIT_QUEUE_SIZE, undertowConfig.getInt(KEY_REQ_LIMIT_QUEUE_SIZE));
        RequestLimit limit = new RequestLimit(maxConcurrentRequests, queueSize);
        RequestLimitingHandler requestLimitingHandler = Handlers.requestLimitingHandler(limit, allowedMethodsHandler);
        return Handlers.gracefulShutdown(requestLimitingHandler);
    }

    private Set<HttpString> allowedMethods(Config mainConfig) {
        return mainConfig.getStringList(KEY_ALLOWED_METHODS)
                .stream()
                .map(HttpString::tryFromString)
                .collect(Collectors.toSet());
    }

    private List<ErrorPage> errorPages(Config mainConfig) {
        return mainConfig.getIntList(KEY_ERROR_HANDLER_CODES)
                .stream()
                .map(code -> Servlets.errorPage(mainConfig.getString(KEY_ERROR_HANDLER_PATH), code))
                .collect(Collectors.toList());
    }

    private ServletContainerInitializerInfo sciInfo(SciInfo sciInfo) {
        return new ServletContainerInitializerInfo(sciInfo.getSciClass(), sciInfo.getHandleTypes());
    }

    private SecurityConstraint securityConstraint(Config mainConfig, Config undertowConfig) {
        return Servlets.securityConstraint()
                .addRolesAllowed(mainConfig.getStringList(KEY_AUTH_ROLES))
                .addWebResourceCollection(Servlets.webResourceCollection()
                        .addUrlPatterns(mainConfig.getStringList(KEY_PROTECTED_PATHS))
                        .addHttpMethods(undertowConfig.getStringList(KEY_PROTECTED_PATHS_SECURED_FOR_METHODS)));
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

    private MultipartConfigElement defaultMultipartConfig(Config undertowConfig) {
        return Servlets.multipartConfig(undertowConfig.getString(KEY_MULTIPART_FILE_LOCATION),
                undertowConfig.getLong(KEY_MULTIPART_MAX_FILE_SIZE),
                undertowConfig.getLong(KEY_MULTIPART_MAX_REQUEST_SIZE),
                undertowConfig.getInt(KEY_MULTIPART_FILE_SIZE_THRESHOLD));
    }

    private int sessionTimeout(Config mainConfig) {
        return Integer.getInteger(SYS_PROP_SESSION_TIMEOUT, mainConfig.getInt(KEY_SESSION_TIMEOUT));
    }

    private ServletSessionConfig sessionConfig(Config mainConfig) {
        return new ServletSessionConfig().setHttpOnly(mainConfig.getBoolean(KEY_HTTP_ONLY));
    }

    private DeploymentInfo deploymentInfo(Config mainConfig, Config undertowConfig, ServletDeployment deployment) {
        return Servlets.deployment()
                .setDeploymentName(DEPLOYMENT_NAME)
                .setContextPath(mainConfig.getString(KEY_CONTEXT_PATH))
                .setClassLoader(this.getClass().getClassLoader())
                .addServletContainerInitializer(this.sciInfo(deployment.getSciInfo()))
                .setIgnoreFlush(mainConfig.getBoolean(KEY_IGNORE_FLUSH))
                .setDefaultEncoding(mainConfig.getString(KEY_DEFAULT_ENCODING))
                .setDefaultSessionTimeout(this.sessionTimeout(mainConfig))
                .setChangeSessionIdOnLogin(mainConfig.getBoolean(KEY_CHANGE_SESSION_ID_ON_LOGIN))
                .setInvalidateSessionOnLogout(mainConfig.getBoolean(KEY_INVALIDATE_SESSION_ON_LOGOUT))
                .setIdentityManager(new SimpleIdentityManager(this.getUserManager(), mainConfig))
                .setUseCachedAuthenticationMechanism(undertowConfig.getBoolean(KEY_USE_CACHED_AUTH_MECHANISM))
                .setLoginConfig(Servlets.loginConfig(FORM_AUTH, REALM, ADMIN_LOGIN_URI, ADMIN_LOGIN_URI))
                .addSecurityConstraint(this.securityConstraint(mainConfig, undertowConfig))
                .addServlets(this.servlets(deployment))
                .addErrorPages(this.errorPages(mainConfig))
                .setDefaultMultipartConfig(this.defaultMultipartConfig(undertowConfig))
                .addInitialHandlerChainWrapper(new ServletInitialHandlerWrapper())
                .setServletSessionConfig(this.sessionConfig(mainConfig))
                .setCrawlerSessionManagerConfig(new CrawlerSessionManagerConfig());
    }
}
