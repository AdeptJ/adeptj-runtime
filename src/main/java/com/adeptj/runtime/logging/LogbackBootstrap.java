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
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.Level.toLevel;
import static com.adeptj.runtime.common.Times.elapsedSinceMillis;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * This Class initializes the LOGBACK logging framework. Usually LOGBACK is initialized via logback.xml file on CLASSPATH.
 * But using that approach LOGBACK takes longer to initializes(5+ seconds) which is reduced drastically to under 200 milliseconds
 * using programmatic approach. This is huge improvement on total startup time.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackBootstrap {

    private static final String ROOT_LOG_LEVEL = "root-log-level";

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
    
    // Utility methods only.
    private LogbackBootstrap() {
    }

    @SuppressWarnings("unchecked")
    public static void startLoggerContext() {
        long startTime = System.nanoTime();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Config config = Configs.DEFAULT.logging();
        // Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = consoleAppender(context, config);
        // File Appender
        RollingFileAppender<ILoggingEvent> fileAppender = fileAppender(context, config);
        // Rolling Policy
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = rollingPolicy(config, context);
        rollingPolicy.setParent(fileAppender);
        // Triggering Policy
        SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = triggeringPolicy(config);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        rollingPolicy.start();
        // Set Rolling and Triggering Policy to RollingFileAppender
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        fileAppender.start();
        List<Appender<ILoggingEvent>> appenderList = new ArrayList<>();
        appenderList.add(consoleAppender);
        appenderList.add(fileAppender);
        // initialize all the required loggers.
        rootLogger(context, consoleAppender, config);
        config.getObject(KEY_LOGGERS).unwrapped().forEach((String key, Object val) -> {
            addLogger(Map.class.cast(val), context, appenderList);
        });
        context.start();
        context.getLogger(LogbackBootstrap.class).info("Logback initialized in [{}] ms!!", elapsedSinceMillis(startTime));
    }

    public static void stopLoggerContext() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }

    private static void rootLogger(LoggerContext context, ConsoleAppender<ILoggingEvent> consoleAppender, Config config) {
        // initialize ROOT Logger at specified level which just logs to ConsoleAppender.
        Logger root = context.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(toLevel(config.getString(ROOT_LOG_LEVEL)));
        root.addAppender(consoleAppender);
    }

    private static void addLogger(Map<String, Object> configs, LoggerContext context, List<Appender<ILoggingEvent>> appenderList) {
        Logger logger = context.getLogger((String) configs.get(KEY_LOG_NAME));
        logger.setLevel(toLevel((String) configs.get(KEY_LOG_LEVEL)));
        logger.setAdditive((Boolean) configs.get(KEY_LOG_ADDITIVITY));
        appenderList.forEach(logger::addAppender);
    }

    private static SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy(Config config) {
        SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = new SizeAndTimeBasedFNATP<>();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(config.getString(KEY_LOG_MAX_SIZE)));
        return triggeringPolicy;
    }

    private static TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy(Config config, LoggerContext context) {
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(config.getString(KEY_ROLLOVER_SERVER_LOG_FILE));
        rollingPolicy.setMaxHistory(config.getInt(KEY_LOG_MAX_HISTORY));
        return rollingPolicy;
    }

    private static RollingFileAppender<ILoggingEvent> fileAppender(LoggerContext context, Config config) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setName(APPENDER_FILE);
        fileAppender.setFile(config.getString(KEY_SERVER_LOG_FILE));
        fileAppender.setAppend(true);
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