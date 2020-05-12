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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static ch.qos.logback.classic.Level.toLevel;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

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
public final class LogbackInitializer {

    private static final String KEY_ROOT_LOG_LEVEL = "root-log-level";

    private static final String KEY_SERVER_LOG_FILE = "server-log-file";

    private static final String KEY_ROLLOVER_SERVER_LOG_FILE = "rollover-server-log-file";

    private static final String KEY_FILE_APPENDER_NAME = "file-appender-name";

    private static final String KEY_ASYNC_APPENDER_NAME = "async-appender-name";

    private static final String KEY_LOG_PATTERN_FILE = "log-pattern-file";

    private static final String KEY_LOG_MAX_HISTORY = "log-max-history";

    private static final String KEY_LOG_MAX_SIZE = "log-max-size";

    private static final String KEY_LOGGERS = "loggers";

    private static final String KEY_LOG_NAME = "name";

    private static final String KEY_LOG_LEVEL = "level";

    private static final String KEY_LOG_ADDITIVITY = "additivity";

    private static final String INIT_MSG = "Logback initialized in [{}] ms!!";

    private static final String SYS_PROP_LOG_ASYNC = "log.async";

    private static final String KEY_ASYNC_LOG_QUEUE_SIZE = "async-log-queue-size";

    private static final String KEY_ASYNC_LOG_DISCARD_THRESHOLD = "async-log-discardingThreshold";

    private static final String KEY_IMMEDIATE_FLUSH = "file-appender-immediate-flush";

    private static final String LOGGER_NAME = "com.adeptj.runtime.logging.LogbackInitializer";

    // Utility methods only.
    private LogbackInitializer() {
    }

    public static void init() {
        long startTime = System.nanoTime();
        Config loggingCfg = Configs.of().logging();
        LogbackManager logbackMgr = LogbackManagerHolder.getInstance().getLogbackManager();
        // Create ConsoleAppender.
        ConsoleAppender<ILoggingEvent> consoleAppender = logbackMgr.newConsoleAppender(loggingCfg);
        logbackMgr.addConsoleAppender(consoleAppender);
        // Create RollingFileAppender.
        LogbackConfig logbackConfig = newRollingFileAppenderConfig(loggingCfg);
        RollingFileAppender<ILoggingEvent> fileAppender = logbackMgr.newRollingFileAppender(logbackConfig);
        logbackMgr.addRollingFileAppender(fileAppender);
        // Add RollingFileAppender Async support.
        addAsyncAppender(loggingCfg, logbackMgr, fileAppender);
        // Initialize Root Logger
        LoggerContext context = logbackMgr.getLoggerContext();
        Logger root = context.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(toLevel(loggingCfg.getString(KEY_ROOT_LOG_LEVEL)));
        root.addAppender(consoleAppender);
        // Add all the loggers defined in server.conf logging section.
        addLoggers(loggingCfg, logbackMgr);
        // SLF4J JUL Bridge.
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        // LevelChangePropagator - see http://logback.qos.ch/manual/configuration.html#LevelChangePropagator
        addLevelChangePropagator(context);
        // Finally start LoggerContext and print status information.
        context.start();
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        context.getLogger(LOGGER_NAME).info(INIT_MSG, Times.elapsedMillis(startTime));
    }

    private static LogbackConfig newRollingFileAppenderConfig(Config loggingCfg) {
        return LogbackConfig.builder()
                .appenderName(loggingCfg.getString(KEY_FILE_APPENDER_NAME))
                .logFile(loggingCfg.getString(KEY_SERVER_LOG_FILE))
                .pattern(loggingCfg.getString(KEY_LOG_PATTERN_FILE))
                .immediateFlush(loggingCfg.getBoolean(KEY_IMMEDIATE_FLUSH))
                .logMaxSize(loggingCfg.getString(KEY_LOG_MAX_SIZE))
                .rolloverFile(loggingCfg.getString(KEY_ROLLOVER_SERVER_LOG_FILE))
                .logMaxHistory(loggingCfg.getInt(KEY_LOG_MAX_HISTORY))
                .build();
    }

    private static void addAsyncAppender(Config loggingCfg, LogbackManager logbackMgr, Appender<ILoggingEvent> appender) {
        if (Boolean.getBoolean(SYS_PROP_LOG_ASYNC)) {
            logbackMgr.addAsyncAppender(LogbackConfig.builder()
                    .asyncAppenderName(loggingCfg.getString(KEY_ASYNC_APPENDER_NAME))
                    .asyncLogQueueSize(loggingCfg.getInt(KEY_ASYNC_LOG_QUEUE_SIZE))
                    .asyncLogDiscardingThreshold(loggingCfg.getInt(KEY_ASYNC_LOG_DISCARD_THRESHOLD))
                    .asyncAppender(appender)
                    .build());
        }
    }

    private static void addLoggers(Config loggingCfg, LogbackManager logbackMgr) {
        for (Config loggerCfg : loggingCfg.getConfigList(KEY_LOGGERS)) {
            logbackMgr.addLogger(LogbackConfig.builder()
                    .logger(loggerCfg.getString(KEY_LOG_NAME))
                    .level(loggerCfg.getString(KEY_LOG_LEVEL))
                    .additivity(loggerCfg.getBoolean(KEY_LOG_ADDITIVITY))
                    .build());
        }
    }

    private static void addLevelChangePropagator(LoggerContext context) {
        LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setResetJUL(true);
        propagator.setContext(context);
        propagator.start();
        context.addListener(propagator);
    }
}