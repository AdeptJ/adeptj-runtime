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
package com.adeptj.runtime.server;

import com.adeptj.runtime.common.Constants;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.logging.LogbackBootstrap;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ShutdownHook for graceful server shutdown, this first cleans up the deployment and then stops UNDERTOW server.
 * <p>
 * @author Rakesh.Kumar, AdeptJ
 */
final class ShutdownHook extends Thread {

    private Undertow server;

    private DeploymentManager manager;

    ShutdownHook(Undertow server, DeploymentManager manager) {
        super(Constants.SHUTDOWN_HOOK_THREAD_NAME);
        this.server = server;
        this.manager = manager;
    }

    /**
     * Handles Graceful server shutdown and resource cleanup.
     */
    @Override
    public void run() {
        long startTime = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
        logger.info("Stopping AdeptJ Runtime!!");
        try {
            this.manager.stop();
            this.manager.undeploy();
            this.server.stop();
            logger.info("AdeptJ Runtime stopped in [{}] ms!!", Times.elapsedSinceMillis(startTime));
            ServerLogsExecutors.INSTANCE.shutdownExecutorService();
        } catch (Exception ex) { // NOSONAR
            logger.error("Exception while stopping AdeptJ Runtime!!", ex);
        } finally {
            // Let the LOGBACK cleans up it's state.
            LogbackBootstrap.stopLoggerContext();
        }
    }

}
