package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.FilterInfo;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.typesafe.config.Config;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.VersionLoggerListener;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class TomcatServer extends AbstractServer {

    private Tomcat tomcat;

    private StandardContext context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.TOMCAT;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
        Config config = ConfigProvider.getInstance().getReferenceConfig();
        this.tomcat = new Tomcat();
        this.tomcat.setPort(this.resolvePort(config));
        this.tomcat.setBaseDir("tomcat-deployment");
        this.tomcat.getServer().addLifecycleListener(new VersionLoggerListener());
        Connector connector = this.tomcat.getConnector();
        ProtocolHandler ph = connector.getProtocolHandler();
        if (ph instanceof Http11NioProtocol) {
            ((Http11NioProtocol) ph).setRelaxedPathChars("[]|");
        }
        this.context = (StandardContext) tomcat.addContext("", new File(".").getAbsolutePath());
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(sciInfo.getSciInstance(), sciInfo.getHandleTypes());
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context, this.getUserManager());
        new GeneralConfigurer().configure(this.context);
        Tomcat.addDefaultMimeTypeMappings(this.context);
        try {
            this.tomcat.start();
        } catch (LifecycleException e) {
            this.logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        // Needed by Tomcat's DefaultServlet for serving static content from adeptj-runtime jar.
        this.addJarResourceSet(this.context);
    }

    @Override
    public void stop() {
        try {
            super.preStop();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
        try {
            this.tomcat.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postStart() {
        super.postStart();
        this.tomcat.getServer().await();
    }

    @Override
    protected void doRegisterServlet(ServletInfo info) {
        Tomcat.addServlet(this.context, info.getServletName(), info.getServletInstance());
        this.context.addServletMappingDecoded(info.getPath(), info.getServletName());
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

    private void addJarResourceSet(Context context) {
        String docBase = context.getDocBase();
        File[] jars = new File(docBase.substring(0, docBase.length() - 1) + "/lib").listFiles();
        if (jars == null) {
            return;
        }
        Stream.of(jars)
                .filter(jar -> jar.getName().startsWith("adeptj-runtime") && jar.getName().split("-").length == 3)
                .findAny()
                .ifPresent(jar -> {
                    JarResourceSet resourceSet = new JarResourceSet();
                    resourceSet.setBase(jar.getAbsolutePath());
                    resourceSet.setInternalPath("/WEB-INF");
                    resourceSet.setWebAppMount("/");
                    context.getResources().addJarResources(resourceSet);
                });
    }
}