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
import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServerShutdownHook;
import com.adeptj.runtime.kernel.util.IOUtils;
import com.adeptj.runtime.kernel.util.Times;
import com.adeptj.runtime.osgi.FrameworkManager;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

import static com.adeptj.runtime.common.Constants.ATTRIBUTE_BUNDLE_CONTEXT;
import static com.adeptj.runtime.common.Constants.BANNER_TXT;
import static com.adeptj.runtime.common.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.common.Constants.MV_CREDENTIALS_STORE;
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

    private static final String KEY_USER_CREDENTIAL_MAPPING = "common.user-credential-mapping";

    private static final String LOGBACK_INIT_MSG = "Logback initialized in [{}] ms!!";

    private static final int PWD_START_INDEX = 9;

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
        // This call will initialize the whole logging system.
        Logger logger = LoggerFactory.getLogger(Launcher.class);
        logger.info(LOGBACK_INIT_MSG, Times.elapsedMillis(startTime));
        Launcher launcher = new Launcher();
        launcher.printBanner(logger);
        try {
            logger.info("JRE: [{}], Version: [{}]", JAVA_RUNTIME_NAME, JAVA_RUNTIME_VERSION);
            AbstractServer server = (AbstractServer) ServiceLoader.load(Server.class).iterator().next();
            server.setServerPostStopTask(new LoggerCleanupTask());
            ServerRuntime runtime = server.getRuntime();
            logger.info("Initializing AdeptJ Runtime based on {}.", runtime.getName());
            launcher.populateCredentialsStore(ConfigProvider.getInstance().getMainConfig());
            ServerBootstrapper bootstrapper = ServerBootstrapperResolver.resolve(runtime);
            bootstrapper.bootstrap(server, args);
            // OSGi Framework is initialized by this time and BundleContext is available as well.
            server.addServletContextAttribute(ATTRIBUTE_BUNDLE_CONTEXT, BundleContextHolder.getInstance().getBundleContext());
            Runtime.getRuntime().addShutdownHook(new ServerShutdownHook(server, "AdeptJ Terminator"));
            logger.info("AdeptJ Runtime initialized in [{}] ms!!", Times.elapsedMillis(startTime));
            server.postStart();
        } catch (Throwable th) { // NOSONAR
            logger.error("Exception while initializing AdeptJ Runtime!!", th);
            launcher.cleanup(logger);
        }
    }

    private void cleanup(Logger logger) {
        // Check if OSGi Framework was already started, try to stop the framework gracefully.
        if (BundleContextHolder.getInstance().getBundleContext() != null) {
            logger.warn("Server startup failed but OSGi Framework already started, stopping it gracefully!!");
            FrameworkManager.getInstance().stopFramework();
        }
        LogbackManagerHolder.getInstance().getLogbackManager().cleanup();
        System.exit(-1); // NOSONAR
    }

    private void printBanner(Logger logger) {
        try (InputStream stream = this.getClass().getResourceAsStream(BANNER_TXT)) {
            logger.info(IOUtils.toString(stream)); // NOSONAR
        } catch (IOException ex) {
            // Just debug log it, it's not critical.
            logger.debug("Exception while printing server banner!!", ex);
        }
    }

    private void populateCredentialsStore(Config mainConfig) {
        try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
            MVMap<String, String> credentials = store.openMap(H2_MAP_ADMIN_CREDENTIALS);
            // put the default password only when it is not set from web console.
            mainConfig.getObject(KEY_USER_CREDENTIAL_MAPPING)
                    .entrySet()
                    .stream()
                    .filter(entry -> StringUtils.isEmpty(credentials.get(entry.getKey())))
                    .forEach(entry -> credentials.put(entry.getKey(), ((String) entry.getValue().unwrapped())
                            .substring(PWD_START_INDEX)));
        }
    }
}