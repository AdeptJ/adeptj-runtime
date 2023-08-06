package com.adeptj.runtime.core;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.servlet.AdminServlet;
import com.adeptj.runtime.servlet.ErrorServlet;
import com.typesafe.config.Config;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.LOGBACK_VIEW_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.LOGBACK_VIEW_SERVLET_URI;
import static com.adeptj.runtime.kernel.ServerRuntime.UNDERTOW;

/**
 * Bootstrap the given {@link Server} instance.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public final class ServerBootstrapper {

    /**
     * Bootstrap the given {@link Server} instance.
     *
     * @param server    the {@link Server} (Tomcat, Jetty, Undertow) instance.
     * @param appConfig the application configuration.
     * @param args      the program arguments to the server instance.
     */
    public static void bootstrap(Server server, Config appConfig, String[] args) throws Exception {
        Set<Class<?>> handleTypes = new LinkedHashSet<>();
        handleTypes.add(FrameworkLauncher.class);
        handleTypes.add(DefaultStartupAware.class);
        SciInfo sciInfo;
        // Undertow expects a ServletContainerInitializer class instance.
        if (server.getRuntime() == UNDERTOW) {
            sciInfo = new SciInfo(RuntimeInitializer.class, handleTypes);
        } else {
            sciInfo = new SciInfo(new RuntimeInitializer(), handleTypes);
        }
        ServletDeployment deployment = new ServletDeployment(sciInfo)
                .addServletInfo(new ServletInfo(ADMIN_SERVLET_NAME, ADMIN_SERVLET_URI, AdminServlet.class))
                .addServletInfo(new ServletInfo(ERROR_SERVLET_NAME, ERROR_SERVLET_URI, ErrorServlet.class))
                .addServletInfo(new ServletInfo(LOGBACK_VIEW_SERVLET_NAME, LOGBACK_VIEW_SERVLET_URI,
                        ViewStatusMessagesServlet.class));
        server.start(deployment, appConfig, args);
    }
}
