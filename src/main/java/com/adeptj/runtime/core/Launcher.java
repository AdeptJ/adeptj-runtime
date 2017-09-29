/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.logging.LogbackManager;
import com.adeptj.runtime.osgi.OSGiManager;
import com.adeptj.runtime.server.CoreServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.adeptj.runtime.common.Constants.REGEX_EQ;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_VERSION;

/**
 * Entry point for launching the AdeptJ Runtime.
 * <p>
 * Rakesh.Kumar, AdeptJ
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
        // First of all initialize Logback.
        LogbackManager.startLogback();
        Logger logger = LoggerFactory.getLogger(Launcher.class);
        try {
            logger.info("JRE: [{}], Version: [{}]", JAVA_RUNTIME_NAME, JAVA_RUNTIME_VERSION);
            CoreServer.bootstrap(parseCommands(args));
            logger.info("AdeptJ Runtime initialized in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable th) { // NOSONAR
            logger.error("Exception while initializing AdeptJ Runtime!!", th);
            if (Boolean.getBoolean(SYS_PROP_ENABLE_SYSTEM_EXIT)) {
                stopOSGiFramework(logger);
                logger.error("Shutting down JVM!!", th);
                // Let the LOGBACK cleans up it's state.
                LogbackManager.stopLogback();
                System.exit(-1);
            }
        }
    }

    private static void stopOSGiFramework(Logger logger) {
        // Check if OSGi Framework was already started, try to stop the framework gracefully.
        if (BundleContextHolder.INSTANCE.isBundleContextAvailable()) {
            logger.warn("Server startup failed but OSGi Framework was started already, stopping it gracefully!!");
            OSGiManager.INSTANCE.stopFramework();
        }
    }

    private static Map<String, String> parseCommands(String[] commands) {
        return Arrays.stream(commands)
                .map(cmd -> cmd.split(REGEX_EQ))
                .collect(Collectors.toMap(cmdArray -> cmdArray[0], cmdArray -> cmdArray[1]));
    }

}