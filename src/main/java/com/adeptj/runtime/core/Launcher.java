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
import com.adeptj.runtime.common.Environment;
import com.adeptj.runtime.common.Lifecycle;
import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.common.ShutdownHook;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.osgi.FrameworkManager;
import com.adeptj.runtime.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.adeptj.runtime.common.Constants.ARG_OPEN_CONSOLE;
import static com.adeptj.runtime.common.Constants.KEY_HTTP;
import static com.adeptj.runtime.common.Constants.KEY_PORT;
import static com.adeptj.runtime.common.Constants.OSGI_CONSOLE_URL;
import static com.adeptj.runtime.common.Constants.REGEX_EQ;
import static com.adeptj.runtime.common.Constants.SERVER_STOP_THREAD_NAME;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_VERSION;

/**
 * Entry point for launching the AdeptJ Runtime.
 * <p>
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Launcher {

    private static final String SYS_PROP_ENABLE_SYSTEM_EXIT = "enable.system.exit";

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
        try {
            pauseForDebug();
            logger.info("JRE: [{}], Version: [{}]", JAVA_RUNTIME_NAME, JAVA_RUNTIME_VERSION);
            Map<String, String> runtimeArgs = parseArgs(args);
            Lifecycle lifecycle = new Server();
            lifecycle.start(runtimeArgs);
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(lifecycle, SERVER_STOP_THREAD_NAME));
            launchBrowser(runtimeArgs);
            logger.info("AdeptJ Runtime initialized in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable th) { // NOSONAR
            logger.error("Exception while initializing AdeptJ Runtime!!", th);
            shutdownJvm();
        }
    }

    private static void pauseForDebug() throws InterruptedException {
        // Useful for debugging the server startup in development mode.
        if (Environment.isDev()) {
            Integer waitTime = Integer.getInteger("wait.time.for.debug.attach", 10);
            LoggerFactory.getLogger(Launcher.class).info("Waiting [{}] seconds for debugger to attach!", waitTime);
            TimeUnit.SECONDS.sleep(waitTime);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        return Stream.of(args)
                .map(cmd -> cmd.split(REGEX_EQ))
                .collect(toMap(cmdArray -> cmdArray[0], cmdArray -> cmdArray[1]));
    }

    private static void launchBrowser(Map<String, String> commands) {
        if (Boolean.parseBoolean(commands.get(ARG_OPEN_CONSOLE))) {
            try {
                Environment.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL,
                        Configs.of().undertow().getConfig(KEY_HTTP).getInt(KEY_PORT))));
            } catch (IOException ex) {
                // Just log it, its okay if browser is not launched.
                LoggerFactory.getLogger(Launcher.class).error("Exception while launching browser!!", ex);
            }
        }
    }

    private static void shutdownJvm() {
        Logger logger = LoggerFactory.getLogger(Launcher.class);
        // Check if OSGi Framework was already started, try to stop the framework gracefully.
        Optional.ofNullable(BundleContextHolder.getInstance().getBundleContext())
                .ifPresent(context -> {
                    logger.warn("Server startup failed but OSGi Framework already started, stopping it gracefully!!");
                    FrameworkManager.getInstance().stopFramework();
                });
        // Let the LOGBACK cleans up it's state.
        SLF4JBridgeHandler.uninstall();
        LogbackManagerHolder.getInstance().getLogbackManager().stopLoggerContext();
        if (Boolean.getBoolean(SYS_PROP_ENABLE_SYSTEM_EXIT)) {
            System.exit(-1);
        }
    }
}