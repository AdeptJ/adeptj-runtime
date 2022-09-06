package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.typesafe.config.Config;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.VersionLoggerListener;
import org.apache.catalina.webresources.JarResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_BASE_DIR;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_CTX_PATH;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_DOC_BASE;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_JAR_RES_INTERNAL_PATH;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_JAR_RES_WEBAPP_MT;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_LIB_PATH;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_MAIN_COMMON;
import static com.adeptj.runtime.tomcat.Constants.CFG_KEY_WEBAPP_JAR_NAME;
import static com.adeptj.runtime.tomcat.Constants.SYMBOL_DASH;

public class TomcatServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatServer.class);

    private Tomcat tomcat;

    private StandardContext context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.TOMCAT;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
        try {
            Config appConfig = ConfigProvider.getInstance().getApplicationConfig();
            Config serverConfig = appConfig.getConfig(this.getRuntime().getName().toLowerCase());
            this.tomcat = new Tomcat();
            this.tomcat.setBaseDir(serverConfig.getString(CFG_KEY_BASE_DIR));
            this.tomcat.getServer().addLifecycleListener(new VersionLoggerListener());
            int port = this.resolvePort(appConfig);
            new ConnectorConfigurer().configure(port, this.tomcat, serverConfig);
            String contextPath = serverConfig.getString(CFG_KEY_CTX_PATH);
            String docBase = serverConfig.getString(CFG_KEY_DOC_BASE);
            this.context = (StandardContext) this.tomcat.addContext(contextPath, new File(docBase).getAbsolutePath());
            SciInfo sciInfo = deployment.getSciInfo();
            this.context.addServletContainerInitializer(sciInfo.getSciInstance(), sciInfo.getHandleTypes());
            this.registerServlets(deployment.getServletInfos());
            new SecurityConfigurer().configure(this.context, this.getUserManager(), appConfig.getConfig(CFG_KEY_MAIN_COMMON));
            new GeneralConfigurer().configure(this.context, serverConfig);
            Tomcat.addDefaultMimeTypeMappings(this.context);
            this.tomcat.start();
            // Needed by Tomcat's DefaultServlet for serving static content from adeptj-runtime jar.
            this.addJarResourceSet(this.context, serverConfig);
        } catch (Exception e) { // NOSONAR
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeInitializationException(e);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void doStop() throws Exception {
        this.tomcat.stop();
    }

    @Override
    public void postStart() {
        this.tomcat.getServer().await();
    }

    @Override
    protected void doRegisterServlet(ServletInfo info) {
        Tomcat.addServlet(this.context, info.getServletName(), info.getServletInstance());
        this.context.addServletMappingDecoded(info.getPath(), info.getServletName());
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

    private void addJarResourceSet(Context context, Config serverConfig) {
        String libPath = serverConfig.getString(CFG_KEY_LIB_PATH);
        String docBase = context.getDocBase();
        File[] jars = new File(docBase.substring(0, docBase.length() - 1) + libPath).listFiles();
        if (jars == null) {
            return;
        }
        String webappJarName = serverConfig.getString(CFG_KEY_WEBAPP_JAR_NAME);
        Stream.of(jars).filter(jar -> jar.getName().startsWith(webappJarName) && jar.getName().split(SYMBOL_DASH).length == 3).findAny().ifPresent(jar -> {
            JarResourceSet resourceSet = new JarResourceSet();
            resourceSet.setBase(jar.getAbsolutePath());
            resourceSet.setInternalPath(serverConfig.getString(CFG_KEY_JAR_RES_INTERNAL_PATH));
            resourceSet.setWebAppMount(serverConfig.getString(CFG_KEY_JAR_RES_WEBAPP_MT));
            context.getResources().addJarResources(resourceSet);
        });
    }
}