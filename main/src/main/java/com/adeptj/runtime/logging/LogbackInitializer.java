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

package com.adeptj.runtime.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.StatusPrinter2;
import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.typesafe.config.Config;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.adeptj.runtime.common.Constants.LOGGING_CONF_SECTION;

/**
 * This Class initializes the Logback logging framework.
 * <p>
 * Usually Logback is initialized via logback.xml file on classpath but programmatic approach is faster and takes
 * about ~ 140 milliseconds to fully bootstrap Logback.
 * <p>
 * Note: 05-Aug-2023 - {@link ConfiguratorRank} was introduced in Logback v1.4.9
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@ConfiguratorRank(value = ConfiguratorRank.CUSTOM_TOP_PRIORITY)
public final class LogbackInitializer extends ContextAwareBase implements Configurator {

    /**
     * See class description for details.
     *
     * @param context the {@link LoggerContext}
     */
    @Override
    public ExecutionStatus configure(LoggerContext context) {
        Config loggingCfg = ConfigProvider.getInstance().getMainConfig().getConfig(LOGGING_CONF_SECTION);
        LogbackManager logbackManager = new LogbackManager(context);
        LogbackManagerHolder.getInstance().setLogbackManager(logbackManager);
        // Initialize ConsoleAppender.
        logbackManager.initConsoleAppender(loggingCfg);
        // Initialize RollingFileAppender.
        logbackManager.initRollingFileAppender(loggingCfg);
        // Update level and add appenders to ROOT Logger
        logbackManager.configureRootLogger(loggingCfg);
        // Add all the loggers defined in server.conf logging section.
        logbackManager.addServerConfigLoggers(loggingCfg);
        // SLF4J JUL Bridge.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        // LevelChangePropagator - see http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
        logbackManager.initLevelChangePropagator();
        // Finally, start LoggerContext and print status information.
        context.start();
        new StatusPrinter2().printInCaseOfErrorsOrWarnings(context);
        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }
}