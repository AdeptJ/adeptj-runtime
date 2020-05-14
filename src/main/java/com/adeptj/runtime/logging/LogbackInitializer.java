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
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.StatusPrinter;
import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * This Class initializes the Logback logging framework.
 * <p>
 * Usually Logback is initialized via logback.xml logFile on CLASSPATH.
 * But using that approach Logback takes longer to initialize(5+ seconds) which is reduced drastically
 * to ~ 200 milliseconds using programmatic approach.
 * <p>
 * This is huge improvement on total startup time.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class LogbackInitializer extends ContextAwareBase implements Configurator {

    private static final String INIT_MSG = "Logback initialized in [{}] ms!!";

    private static final String LOGGER_NAME = "com.adeptj.runtime.logging.LogbackInitializer";

    /**
     * @param loggerContext the {@link LoggerContext}
     * @see class description for details.
     */
    @Override
    public void configure(LoggerContext loggerContext) {
        long startTime = System.nanoTime();
        Config loggingCfg = Configs.of().logging();
        LogbackManager logbackManager = new LogbackManager(loggerContext);
        LogbackManagerHolder.getInstance().setLogbackManager(logbackManager);
        // Initialize ConsoleAppender.
        logbackManager.initConsoleAppender(loggingCfg);
        // Initialize RollingFileAppender.
        logbackManager.initRollingFileAppender(loggingCfg);
        // Update level and add appenders to ROOT Logger
        logbackManager.changeLevelAndAddAppendersToRootLogger(loggingCfg);
        // Add all the loggers defined in server.conf logging section.
        logbackManager.addConfigLoggers(loggingCfg);
        // SLF4J JUL Bridge.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        // LevelChangePropagator - see http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
        logbackManager.initLevelChangePropagator();
        // Finally start LoggerContext and print status information.
        loggerContext.start();
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        loggerContext.getLogger(LOGGER_NAME).info(INIT_MSG, Times.elapsedMillis(startTime));
    }
}