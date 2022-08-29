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

package com.adeptj.runtime.core;

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServerName;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.osgi.FrameworkManager;
import com.adeptj.runtime.server.DefaultStartupAware;
import com.adeptj.runtime.servlet.AdminServlet;
import com.adeptj.runtime.servlet.ErrorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_VERSION;

/**
 * Entry point for launching the AdeptJ Runtime.
 * <p>
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Launcher {

    // Deny direct instantiation.
    private Launcher() {
    }

    /**
     * Entry point for initializing the AdeptJ Runtime.
     * <p>
     * It does the following tasks in order.
     * <p>
     * 1. Initializes the Logback logging framework.
     * 2. Does the deployment to embedded UNDERTOW.
     * 3. Starts the OSGi Framework.
     * 4. Starts the Undertow server.
     * 5. Registers the runtime ShutdownHook.
     *
     * @param args command line arguments for the Launcher.
     */
    public static void main(String[] args) {
        Thread.currentThread().setName("AdeptJ Launcher");
        long startTime = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(Launcher.class);
        Launcher launcher = new Launcher();
        try {
            logger.info("JRE: [{}], Version: [{}]", JAVA_RUNTIME_NAME, JAVA_RUNTIME_VERSION);
            //Lifecycle lifecycle = new Server();
            //lifecycle.start(args);
            //Runtime.getRuntime().addShutdownHook(new ShutdownHook(lifecycle, SERVER_STOP_THREAD_NAME));
            for (Server server : ServiceLoader.load(Server.class)) {
                logger.info("Found ServiceLoader based Server: [{}]", server);
                try {
                    Set<Class<?>> classes = new LinkedHashSet<>();
                    classes.add(FrameworkLauncher.class);
                    classes.add(DefaultStartupAware.class);
                    if (server.getName() == ServerName.TOMCAT) {
                        ServletDeployment deployment = new ServletDeployment(new SciInfo(new RuntimeInitializer(), classes));
                        ServletInfo adminServletInfo = new ServletInfo("AdeptJ AdminServlet", ADMIN_SERVLET_URI);
                        adminServletInfo.setServletInstance(new AdminServlet());
                        ServletInfo errorServletInfo = new ServletInfo("AdeptJ ErrorServlet", ERROR_SERVLET_URI);
                        errorServletInfo.setServletInstance(new ErrorServlet());
                        deployment.addServletInfo(adminServletInfo).addServletInfo(errorServletInfo);
                        server.start(args, deployment);
                    } else if (server.getName() == ServerName.JETTY) {
                        ServletDeployment deployment = new ServletDeployment(new SciInfo(new RuntimeInitializer(), classes));
                        ServletInfo adminServletInfo = new ServletInfo("AdeptJ AdminServlet", ADMIN_SERVLET_URI);
                        adminServletInfo.setServletClass(AdminServlet.class);
                        ServletInfo errorServletInfo = new ServletInfo("AdeptJ ErrorServlet", ERROR_SERVLET_URI);
                        errorServletInfo.setServletClass(ErrorServlet.class);
                        deployment.addServletInfo(adminServletInfo).addServletInfo(errorServletInfo);
                        server.start(args, deployment);
                    } else if (server.getName() == ServerName.UNDERTOW) {
                        ServletDeployment deployment = new ServletDeployment(new SciInfo(RuntimeInitializer.class, classes));
                        ServletInfo adminServletInfo = new ServletInfo("AdeptJ AdminServlet", ADMIN_SERVLET_URI);
                        adminServletInfo.setServletClass(AdminServlet.class);
                        ServletInfo errorServletInfo = new ServletInfo("AdeptJ ErrorServlet", ERROR_SERVLET_URI);
                        errorServletInfo.setServletClass(ErrorServlet.class);
                        deployment.addServletInfo(adminServletInfo).addServletInfo(errorServletInfo);
                        server.start(args, deployment);
                    }
                    server.postStart();
                } catch (Exception ex) { // NOSONAR
                    logger.error("Exception while executing ServiceLoader based StartupAware#onStartup!!", ex);
                }
            }
            logger.info("AdeptJ Runtime initialized in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable th) { // NOSONAR
            logger.error("Exception while initializing AdeptJ Runtime!!", th);
            launcher.cleanup(logger);
        }
    }

    private void cleanup(Logger logger) {
        // Check if OSGi Framework was already started, try to stop the framework gracefully.
        Optional.ofNullable(BundleContextHolder.getInstance().getBundleContext())
                .ifPresent(context -> {
                    logger.warn("Server startup failed but OSGi Framework already started, stopping it gracefully!!");
                    FrameworkManager.getInstance().stopFramework();
                });
        // Let the LOGBACK cleans up it's state.
        SLF4JBridgeHandler.uninstall();
        LogbackManagerHolder.getInstance().getLogbackManager().stopLogback();
        System.exit(-1); // NOSONAR
    }
}