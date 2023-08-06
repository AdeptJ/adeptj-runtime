package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.typesafe.config.Config;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.VersionLoggerListener;
import org.apache.catalina.webresources.JarResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_BASE_DIR;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_CTX_PATH;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_DOC_BASE;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_JAR_RES_INTERNAL_PATH;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_JAR_RES_WEBAPP_MT;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_LIB_PATH;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_MAIN_COMMON;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_WEBAPP_JAR_NAME;
import static com.adeptj.runtime.tomcat.ServerConstants.SYMBOL_DASH;

public class TomcatServer extends AbstractServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatServer.class);

    private Tomcat tomcat;

    private StandardContext context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.TOMCAT;
    }

    @Override
    public void start(ServletDeployment deployment, Config appConfig, String[] args) throws Exception {
        Config serverConfig = appConfig.getConfig(this.getRuntime().getLowerCaseName());
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
        Config commonConfig = appConfig.getConfig(CFG_KEY_MAIN_COMMON);
        new SecurityConfigurer().configure(this.context, this.getUserManager(), commonConfig);
        new GeneralConfigurer().configure(this.context, commonConfig, serverConfig);
        Tomcat.addDefaultMimeTypeMappings(this.context);
        this.tomcat.start();
        // Needed by Tomcat's DefaultServlet for serving static content from adeptj-runtime jar.
        this.addJarResourceSet(this.context, serverConfig);
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
        Tomcat.addServlet(this.context, info.servletName(), info.servletClass().getName());
        this.context.addServletMappingDecoded(info.path(), info.servletName());
    }

    @Override
    protected void doRegisterFilter(FilterInfo info) {
        // NOP
    }

    @Override
    public void addServletContextAttribute(String name, Object value) {
        this.context.getServletContext().setAttribute(name, value);
    }

    private void addJarResourceSet(Context context, Config serverConfig) {
        String webappRoot = serverConfig.getString(CFG_KEY_JAR_RES_INTERNAL_PATH);
        String webAppMount = serverConfig.getString(CFG_KEY_JAR_RES_WEBAPP_MT);
        String webappJarName = serverConfig.getString(CFG_KEY_WEBAPP_JAR_NAME);
        String docBase = context.getDocBase();
        String libDirPath = docBase.substring(0, docBase.length() - 1) + serverConfig.getString(CFG_KEY_LIB_PATH);
        // Get the adeptj-runtime-x.x.x.jar file from the lib directory.
        File[] jars = new File(libDirPath)
                .listFiles((dir, name) -> name.startsWith(webappJarName) && name.split(SYMBOL_DASH).length == 3);
        // There should be exactly one file in the array.
        if (jars == null || jars.length != 1) {
            LOGGER.error("There are multiple or no adeptj-runtime-x.x.x.jar file present, static resources will not be loaded!!");
            return;
        }
        String base = jars[0].getAbsolutePath();
        LOGGER.info("Static resource base resolved to: [{}]", base);
        this.doAddJarResourceSet(context, base, webappRoot, webAppMount);
    }

    private void doAddJarResourceSet(Context context, String base, String internalPath, String webAppMount) {
        JarResourceSet resourceSet = new JarResourceSet();
        resourceSet.setBase(base);
        resourceSet.setInternalPath(internalPath);
        resourceSet.setWebAppMount(webAppMount);
        context.getResources().addJarResources(resourceSet);
    }
}