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

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.Level.toLevel;
import static com.adeptj.runtime.common.Times.elapsedMillis;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * This Class initializes the Logback logging framework.
 * <p>
 * Usually Logback is initialized via logback.xml file on CLASSPATH.
 * But using that approach Logback takes longer to initializes(5+ seconds) which is reduced drastically
 * to under 200 milliseconds using programmatic approach.
 * <p>
 * This is huge improvement on total startup time.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackManager {

    private static final String KEY_ROOT_LOG_LEVEL = "root-log-level";

    private static final String KEY_SERVER_LOG_FILE = "server-log-file";

    private static final String KEY_ROLLOVER_SERVER_LOG_FILE = "rollover-server-log-file";

    private static final String KEY_LOG_PATTERN_FILE = "log-pattern-file";

    private static final String KEY_LOG_PATTERN_CONSOLE = "log-pattern-console";

    private static final String KEY_LOG_MAX_HISTORY = "log-max-history";

    private static final String KEY_LOG_MAX_SIZE = "log-max-size";

    private static final String KEY_LOGGERS = "loggers";

    private static final String KEY_LOG_NAME = "name";

    private static final String KEY_LOG_LEVEL = "level";

    private static final String KEY_LOG_ADDITIVITY = "additivity";

    private static final String APPENDER_CONSOLE = "CONSOLE";

    private static final String APPENDER_FILE = "FILE";

    private static final String APPENDER_ASYNC = "ASYNC";

    private static final String INIT_MSG = "Logback initialized in [{}] ms!!";

    private static final String SYS_PROP_LOG_ASYNC = "log.async";

    private static final String SYS_PROP_LOG_IMMEDIATE_FLUSH = "log.immediate.flush";

    private static final String KEY_ASYNC_LOG_QUEUE_SIZE = "async-log-queue-size";

    private static final String KEY_ASYNC_LOG_DISCARD_THRESHOLD = "async-log-discardingThreshold";

    private static final String KEY_IMMEDIATE_FLUSH = "file-appender-immediate-flush";

    // Utility methods only.
    private LogbackManager() {
    }

    public static void startLogback() {
        long startTime = System.nanoTime();
        Config config = Configs.DEFAULT.logging();
        LoggerContext context = getLoggerContext();
        // File Appender
        RollingFileAppender<ILoggingEvent> fileAppender = fileAppender(context, config);
        // Triggering & Rolling Policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> trigAndRollPolicy = trigAndRollPolicy(context, config);
        trigAndRollPolicy.setParent(fileAppender);
        trigAndRollPolicy.start();
        // Set Rolling and Triggering Policy to RollingFileAppender
        fileAppender.setRollingPolicy(trigAndRollPolicy);
        fileAppender.setTriggeringPolicy(trigAndRollPolicy);
        fileAppender.start();
        // Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = consoleAppender(context, config);
        List<Appender<ILoggingEvent>> appenderList = new ArrayList<>();
        appenderList.add(consoleAppender);
        appenderList.add(fileAppender);
        // initialize all the required loggers.
        rootLogger(context, consoleAppender, config);
        config.getObject(KEY_LOGGERS)
                .unwrapped()
                .forEach((logCfgName, logCfgMap) -> addLogger(logCfgMap, context, appenderList));
        // AsyncAppender
        asyncAppender(config, context, fileAppender);
        context.start();
        context.getLogger(LogbackManager.class).info(INIT_MSG, elapsedMillis(startTime));
    }

    public static void stopLogback() {
        getLoggerContext().stop();
    }

    private static LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private static void rootLogger(LoggerContext context, ConsoleAppender<ILoggingEvent> appender, Config config) {
        // initialize ROOT Logger at specified level which just logs to ConsoleAppender.
        Logger root = context.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(toLevel(config.getString(KEY_ROOT_LOG_LEVEL)));
        root.addAppender(appender);
    }

    private static void addLogger(Object configMap, LoggerContext context, List<Appender<ILoggingEvent>> appenderList) {
        @SuppressWarnings("unchecked")
        Map<String, Object> configs = Map.class.cast(configMap);
        Logger logger = context.getLogger((String) configs.get(KEY_LOG_NAME));
        logger.setLevel(toLevel((String) configs.get(KEY_LOG_LEVEL)));
        logger.setAdditive((Boolean) configs.get(KEY_LOG_ADDITIVITY));
        appenderList.forEach(logger::addAppender);
    }

    private static SizeAndTimeBasedRollingPolicy<ILoggingEvent> trigAndRollPolicy(LoggerContext context, Config config) {
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> trigAndRollPolicy = new SizeAndTimeBasedRollingPolicy<>();
        trigAndRollPolicy.setMaxFileSize(FileSize.valueOf(config.getString(KEY_LOG_MAX_SIZE)));
        trigAndRollPolicy.setContext(context);
        trigAndRollPolicy.setFileNamePattern(config.getString(KEY_ROLLOVER_SERVER_LOG_FILE));
        trigAndRollPolicy.setMaxHistory(config.getInt(KEY_LOG_MAX_HISTORY));
        return trigAndRollPolicy;
    }

    private static void asyncAppender(Config config, LoggerContext context, Appender<ILoggingEvent> appender) {
        if (Boolean.getBoolean(SYS_PROP_LOG_ASYNC)) {
            AsyncAppender asyncAppender = new AsyncAppender();
            asyncAppender.setName(APPENDER_ASYNC);
            asyncAppender.setQueueSize(config.getInt(KEY_ASYNC_LOG_QUEUE_SIZE));
            asyncAppender.setDiscardingThreshold(config.getInt(KEY_ASYNC_LOG_DISCARD_THRESHOLD));
            asyncAppender.setContext(context);
            asyncAppender.addAppender(appender);
            asyncAppender.start();
        }
    }

    private static RollingFileAppender<ILoggingEvent> fileAppender(LoggerContext context, Config config) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setName(APPENDER_FILE);
        fileAppender.setFile(config.getString(KEY_SERVER_LOG_FILE));
        fileAppender.setAppend(true);
        // First check the system property for immediate log flush, if false then use one from config.
        // Default behaviour is not to flush immediately for performance reasons.
        fileAppender.setImmediateFlush(Boolean.getBoolean(SYS_PROP_LOG_IMMEDIATE_FLUSH));
        if (!fileAppender.isImmediateFlush()) {
            fileAppender.setImmediateFlush(config.getBoolean(KEY_IMMEDIATE_FLUSH));
        }
        fileAppender.setEncoder(layoutEncoder(context, config.getString(KEY_LOG_PATTERN_FILE)));
        fileAppender.setContext(context);
        return fileAppender;
    }

    private static ConsoleAppender<ILoggingEvent> consoleAppender(LoggerContext context, Config config) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName(APPENDER_CONSOLE);
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(layoutEncoder(context, config.getString(KEY_LOG_PATTERN_CONSOLE)));
        consoleAppender.start();
        return consoleAppender;
    }

    private static PatternLayoutEncoder layoutEncoder(LoggerContext context, String logPattern) {
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(context);
        layoutEncoder.setPattern(logPattern);
        layoutEncoder.start();
        return layoutEncoder;
    }
}