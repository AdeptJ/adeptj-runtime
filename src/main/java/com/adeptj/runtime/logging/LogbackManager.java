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
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.classic.Level.INFO;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * LogbackManager
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class LogbackManager {

    private static final String KEY_CONSOLE_APPENDER_NAME = "console-appender-name";

    private static final String KEY_LOG_PATTERN_CONSOLE = "log-pattern-console";

    private static final String SYS_PROP_LOG_IMMEDIATE_FLUSH = "log.immediate.flush";

    private static final String HIGHLIGHT = "highlight";

    private static final String THREAD = "thread";

    private final List<Appender<ILoggingEvent>> appenders;

    private final LoggerContext loggerContext;

    public LogbackManager() {
        this.appenders = new ArrayList<>();
        this.loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayout.defaultConverterMap.put(HIGHLIGHT, DebugLevelHighlightingConverter.class.getName());
        PatternLayout.defaultConverterMap.put(THREAD, TrimThreadNameConverter.class.getName());
    }

    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }

    public void addConsoleAppender(ConsoleAppender<ILoggingEvent> appender) {
        this.appenders.add(appender);
    }

    public void addRollingFileAppender(RollingFileAppender<ILoggingEvent> appender) {
        this.appenders.add(appender);
    }

    public void addLogger(LogbackConfig logbackConfig) {
        Logger logger = this.loggerContext.getLogger(logbackConfig.getLogger());
        logger.setLevel(Level.toLevel(logbackConfig.getLevel()));
        logger.setAdditive(logbackConfig.isAdditivity());
        this.appenders.forEach(appender -> {
            if (!logger.isAttached(appender)) {
                logger.addAppender(appender);
            }
        });
    }

    public void resetLogger(LogbackConfig logbackConfig) {
        Logger logger = this.loggerContext.getLogger(logbackConfig.getLogger());
        logger.setLevel(INFO);
        logger.setAdditive(logbackConfig.isAdditivity());
    }

    ConsoleAppender<ILoggingEvent> newConsoleAppender(Config loggingCfg) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(this.loggerContext);
        consoleAppender.setName(loggingCfg.getString(KEY_CONSOLE_APPENDER_NAME));
        consoleAppender.setEncoder(this.newLayoutEncoder(loggingCfg.getString(KEY_LOG_PATTERN_CONSOLE)));
        consoleAppender.setWithJansi(true);
        consoleAppender.start();
        return consoleAppender;
    }

    RollingFileAppender<ILoggingEvent> newRollingFileAppender(LogbackConfig rollingFileConfig) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(this.loggerContext);
        fileAppender.setName(rollingFileConfig.getAppenderName());
        fileAppender.setFile(rollingFileConfig.getLogFile());
        fileAppender.setAppend(true);
        fileAppender.setEncoder(this.newLayoutEncoder(rollingFileConfig.getPattern()));
        fileAppender.setImmediateFlush(Boolean.getBoolean(SYS_PROP_LOG_IMMEDIATE_FLUSH));
        if (!fileAppender.isImmediateFlush()) {
            fileAppender.setImmediateFlush(rollingFileConfig.isImmediateFlush());
        }
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(this.loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(rollingFileConfig.getLogMaxSize()));
        rollingPolicy.setFileNamePattern(rollingFileConfig.getRolloverFile());
        rollingPolicy.setMaxHistory(rollingFileConfig.getLogMaxHistory());
        rollingPolicy.start();
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(rollingPolicy);
        fileAppender.start();
        return fileAppender;
    }

    private PatternLayoutEncoder newLayoutEncoder(String logPattern) {
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(this.loggerContext);
        layoutEncoder.setPattern(logPattern);
        layoutEncoder.setCharset(UTF_8);
        layoutEncoder.start();
        return layoutEncoder;
    }

    void addAsyncAppender(LogbackConfig logbackConfig) {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(this.loggerContext);
        asyncAppender.setName(logbackConfig.getAsyncAppenderName());
        asyncAppender.setQueueSize(logbackConfig.getAsyncLogQueueSize());
        asyncAppender.setDiscardingThreshold(logbackConfig.getAsyncLogDiscardingThreshold());
        asyncAppender.addAppender(logbackConfig.getAsyncAppender());
        asyncAppender.start();
    }
}
