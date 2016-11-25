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
package com.adeptj.runtime.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.adeptj.runtime.common.TimeUnits;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static ch.qos.logback.classic.Level.toLevel;

/**
 * This Class initializes the LOGBACK logging framework. Usually LOGBACK is initialized via logback.xml file on CLASSPATH.
 * But using that approach LOGBACK takes longer to initializes(5+ seconds) which is reduced drastically to under 150 milliseconds
 * using programmatic approach. This is huge improvement on total startup time.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackProvisioner {

    private static final String ADEPTJ_LOG_LEVEL = "adeptj-log-level";

    private static final String UNDERTOW_LOG_LEVEL = "undertow-log-level";

    private static final String XNIO_LOG_LEVEL = "xnio-log-level";

    private static final String THYMELEAF_LOG_LEVEL = "thymeleaf-log-level";

    private static final String TRIMOU_LOG_LEVEL = "trimou-log-level";

    private static final String ROOT_LOG_LEVEL = "root-log-level";

    private static final String LOGGER_XNIO = "org.xnio";

    private static final String LOGGER_UNDERTOW = "io.undertow";

    private static final String LOGGER_ADEPTJ = "com.adeptj";

    private static final String LOGGER_THYMELEAF = "org.thymeleaf";

    private static final String LOGGER_TRIMOU = "org.trimou";

    private static final String KEY_SERVER_LOG_FILE = "server-log-file";

    private static final String KEY_ROLLOVER_SERVER_LOG_FILE = "rollover-server-log-file";

    private static final String KEY_LOG_PATTERN = "log-pattern";

    private static final String KEY_LOG_MAX_HISTORY = "log-max-history";

    private static final String KEY_LOG_MAX_SIZE = "log-max-size";

    private static final String APPENDER_CONSOLE = "CONSOLE";

    private static final String APPENDER_FILE = "FILE";

    public static void start() {
        long startTime = System.nanoTime();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Config config = Configs.INSTANCE.common();
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
        // initialize all the required loggers.
        rootLogger(context, consoleAppender, config);
        List<Appender<ILoggingEvent>> appenders = Arrays.asList(consoleAppender, fileAppender);
        adeptjLogger(context, appenders, config);
        undertowLogger(context, appenders, config);
        xnioLogger(context, appenders, config);
        // thymeleafLogger(context, appenders, config);
        trimouLogger(context, appenders, config);
        context.start();
        Logger logger = context.getLogger(LogbackProvisioner.class);
        logger.info("Logback initialized in [{}] ms!!", TimeUnits.nanosToMillis(startTime));
    }

    public static void stop() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.stop();
    }

    public static void addLogger(String name, String level, boolean additivity) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(name);
        logger.setLevel(toLevel(level));
        logger.setAdditive(additivity);
    }

    private static void addAppenders(List<Appender<ILoggingEvent>> appenders, Logger logger) {
        for (Appender<ILoggingEvent> appender : appenders) {
            logger.addAppender(appender);
        }
    }

    private static void rootLogger(LoggerContext context, ConsoleAppender<ILoggingEvent> consoleAppender, Config config) {
        // initialize ROOT Logger at specified level.
        Logger root = logger(Logger.ROOT_LOGGER_NAME, toLevel(config.getString(ROOT_LOG_LEVEL)), context);
        root.addAppender(consoleAppender);
    }

    private static void xnioLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config config) {
        Logger xnioLogger = logger(LOGGER_XNIO, toLevel(config.getString(XNIO_LOG_LEVEL)), context);
        addAppenders(appenders, xnioLogger);
        xnioLogger.setAdditive(false);
    }

    private static void undertowLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config config) {
        Logger undertowLogger = logger(LOGGER_UNDERTOW, toLevel(config.getString(UNDERTOW_LOG_LEVEL)), context);
        addAppenders(appenders, undertowLogger);
        undertowLogger.setAdditive(false);
    }

    private static void adeptjLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config config) {
        Logger adeptjLogger = logger(LOGGER_ADEPTJ, toLevel(config.getString(ADEPTJ_LOG_LEVEL)), context);
        addAppenders(appenders, adeptjLogger);
        adeptjLogger.setAdditive(false);
    }

    static void thymeleafLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config config) {
        Logger thymeleafLogger = logger(LOGGER_THYMELEAF, toLevel(config.getString(THYMELEAF_LOG_LEVEL)), context);
        addAppenders(appenders, thymeleafLogger);
        thymeleafLogger.setAdditive(false);
    }

    private static void trimouLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config config) {
        Logger thymeleafLogger = logger(LOGGER_TRIMOU, toLevel(config.getString(TRIMOU_LOG_LEVEL)), context);
        addAppenders(appenders, thymeleafLogger);
        thymeleafLogger.setAdditive(false);
    }

    private static Logger logger(String name, Level level, LoggerContext context) {
        Logger logger = context.getLogger(name);
        logger.setLevel(level);
        return logger;
    }

    private static SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy(Config config) {
        SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = new SizeAndTimeBasedFNATP<>();
        triggeringPolicy.setMaxFileSize(config.getString(KEY_LOG_MAX_SIZE));
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
        fileAppender.setFile(config.getString(KEY_SERVER_LOG_FILE));
        fileAppender.setAppend(true);
        fileAppender.setEncoder(layoutEncoder(context, config.getString(KEY_LOG_PATTERN)));
        fileAppender.setName(APPENDER_FILE);
        fileAppender.setContext(context);
        return fileAppender;
    }

    private static ConsoleAppender<ILoggingEvent> consoleAppender(LoggerContext context, Config config) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName(APPENDER_CONSOLE);
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(layoutEncoder(context, config.getString(KEY_LOG_PATTERN)));
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