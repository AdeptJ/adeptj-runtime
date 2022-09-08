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
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.FileSize;
import com.adeptj.runtime.common.OSGiUtil;
import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import org.apache.commons.lang3.SystemUtils;
import org.osgi.framework.ServiceReference;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.LOGGING_CONF_SECTION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * Manages the Logback bootstrapping at server start and configuring loggers from the OSGi configs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class LogbackManager {

    static final String KEY_ROOT_LOG_LEVEL = "root-log-level";

    private static final String KEY_CONSOLE_APPENDER_NAME = "console-appender-name";

    private static final String KEY_LOG_PATTERN_CONSOLE = "log-pattern-console";

    private static final String KEY_LOGGERS = "loggers";

    private static final String KEY_LOGGER_NAME = "name";

    private static final String KEY_LOGGER_LEVEL = "level";

    private static final String KEY_LOGGER_ADDITIVITY = "additivity";

    private static final String KEY_SERVER_LOG_FILE = "server-log-file";

    private static final String KEY_ROLLOVER_SERVER_LOG_FILE = "rollover-server-log-file";

    private static final String KEY_FILE_APPENDER_NAME = "file-appender-name";

    private static final String KEY_ASYNC_APPENDER_NAME = "async-appender-name";

    private static final String KEY_LOG_PATTERN_FILE = "log-pattern-file";

    private static final String KEY_LOG_MAX_HISTORY = "log-max-history";

    private static final String KEY_LOG_MAX_SIZE = "log-max-size";

    private static final String KEY_ASYNC_LOG_QUEUE_SIZE = "async-log-queue-size";

    private static final String KEY_ASYNC_LOG_DISCARD_THRESHOLD = "async-log-discardingThreshold";

    private static final String KEY_IMMEDIATE_FLUSH = "file-appender-immediate-flush";

    private static final String KEY_OSGI_LOGGER_NAMES = "logger.names";

    private static final String KEY_OSGI_LOGGER_LEVEL = "logger.level";

    private static final String KEY_OSGI_LOGGER_ADDITIVITY = "logger.additivity";

    private static final String SYS_PROP_LOG_ASYNC = "log.async";

    private static final String SYS_PROP_LOG_IMMEDIATE_FLUSH = "log.immediate.flush";

    private static final String HIGHLIGHT_EXT = "highlight_ext";

    private static final String LC_NAME = "AdeptJ";

    private static final String CATEGORY_ALREADY_DEFINED_MSG = "Category [{}] already defined by configuration {}";

    private static final String EMPTY_LOGGER_NAMES_MSG = "Can't add loggers because logger.names array property is empty!!";

    private static final String ROOT_PROHIBITED_MSG = "Adding a ROOT logger is prohibited!!";

    private static final String ROOT_REMOVED_MSG = "Removed ROOT logger from the categories as adding a ROOT logger is prohibited!!";

    private static final String ADDING_LOGGERS_MSG = "Adding loggers for categories {} with level {}";

    private static final String NO_LOGGER_CFG_FOR_PID_MSG = "No logger config found for given pid: {}";

    private static final String REMOVING_LOGGERS_MSG = "Removing loggers for categories {} with level {}";

    private static final String RESETTING_LC_MSG = "Resetting LoggerContext %s";

    private static final String APPENDERS_REINITIALIZED_MSG = "ConsoleAppender and RollingFileAppender reinitialized!";

    private static final String ROOT_REINITIALIZED_MSG = "ROOT Logger reinitialized!";

    private static final String OSGI_LOGGERS_RECONFIGURED_MSG = "Reconfigured loggers for categories {} with level {}";

    private static final String SERVER_CONFIG_LOGGERS_RECONFIGURED_MSG = "Server config loggers reconfigured!";

    private static final String RESET_LC_DONE_MSG = "Reset of LoggerContext %s done!";

    private static final String LC_RESET_TIME_MSG = "LoggerContext reset took [{}] ms!";

    private ConsoleAppender<ILoggingEvent> consoleAppender;

    private RollingFileAppender<ILoggingEvent> fileAppender;

    private final LoggerContext loggerContext;

    private final ContextUtil contextUtil;

    // This map instance is only initialized when logger config is added first time from OSGi.
    private Map<String, LoggerConfig> configByPid;

    LogbackManager(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        this.loggerContext.setName(LC_NAME);
        this.contextUtil = new ContextUtil(loggerContext);
        PatternLayout.DEFAULT_CONVERTER_MAP.put(HIGHLIGHT_EXT, DebugLevelHighlightingConverter.class.getName());
    }

    public void cleanup() {
        SLF4JBridgeHandler.uninstall();
        // Let Logback cleans up it's state.
        this.loggerContext.stop();
    }

    public void addOSGiLoggers(ServiceReference<?> reference) {
        Set<String> categories = OSGiUtil.arrayToSet(reference, KEY_OSGI_LOGGER_NAMES);
        if (this.validateCategories(categories)) {
            String pid = OSGiUtil.getString(reference, SERVICE_PID);
            String level = OSGiUtil.getString(reference, KEY_OSGI_LOGGER_LEVEL);
            boolean additivity = OSGiUtil.getBoolean(reference, KEY_OSGI_LOGGER_ADDITIVITY);
            this.configByPid.put(pid, new LoggerConfig(pid, categories, level, additivity));
            this.loggerContext.getLogger(this.getClass()).info(ADDING_LOGGERS_MSG, categories, level);
            for (String category : categories) {
                this.addLogger(category, level, additivity);
            }
        }
    }

    public void resetLoggers(ServiceReference<?> reference) {
        Logger logger = this.loggerContext.getLogger(this.getClass());
        String pid = OSGiUtil.getString(reference, SERVICE_PID);
        LoggerConfig config = this.configByPid.remove(pid);
        // If LoggerConfig is null for the given pid then there is no need to reset LoggerContext.
        // It also means that the logger config was never captured for this pid, log and return right away.
        if (config == null) {
            logger.info(NO_LOGGER_CFG_FOR_PID_MSG, pid);
            return;
        }
        long startTime = System.nanoTime();
        logger.info(REMOVING_LOGGERS_MSG, config.getCategories(), config.getLevel());
        this.contextUtil.addInfo(String.format(RESETTING_LC_MSG, this.loggerContext.getName()));
        this.loggerContext.reset();
        this.consoleAppender = null;
        this.fileAppender = null;
        Config loggingCfg = ConfigProvider.getInstance().getMainConfig().getConfig(LOGGING_CONF_SECTION);
        this.initConsoleAppender(loggingCfg);
        this.initRollingFileAppender(loggingCfg);
        this.contextUtil.addInfo(APPENDERS_REINITIALIZED_MSG);
        this.configureRootLogger(loggingCfg);
        this.contextUtil.addInfo(ROOT_REINITIALIZED_MSG);
        // Reconfigure server config loggers.
        this.addServerConfigLoggers(loggingCfg);
        // Reconfigure OSGi loggers.
        this.reconfigureOSGiLoggers();
        this.contextUtil.addInfo(SERVER_CONFIG_LOGGERS_RECONFIGURED_MSG);
        this.contextUtil.addInfo(String.format(RESET_LC_DONE_MSG, this.loggerContext.getName()));
        this.loggerContext.getLogger(this.getClass()).info(LC_RESET_TIME_MSG, Times.elapsedMillis(startTime));
    }

    private boolean validateCategories(Set<String> categories) {
        Logger logger = this.loggerContext.getLogger(this.getClass());
        // If no categories defined then log as warning and return false right away.
        if (categories.isEmpty()) {
            logger.warn(EMPTY_LOGGER_NAMES_MSG);
            return false;
        }
        if (categories.remove(ROOT_LOGGER_NAME)) {
            if (categories.isEmpty()) {
                // Means ROOT was the only element in the category Set, log as warning and return false right away.
                logger.warn(ROOT_PROHIBITED_MSG);
                return false;
            }
            // Means ROOT was one of the element in the category Set, as it is already removed then just log as warning.
            logger.warn(ROOT_REMOVED_MSG);
        }
        // Lazy initialization of configByPid map, don't think we still need a CHM as this method is called
        // under a ReentrantLock in LoggerConfigFactoryListener.
        if (this.configByPid == null) {
            this.configByPid = new HashMap<>();
            // No need to check the categories if configByPid map is initialized first time, return true right away.
            return true;
        }
        for (LoggerConfig config : this.configByPid.values()) {
            // If a category is already defined in current LoggerConfig then log as error and return false right away.
            for (String category : config.getCategories()) {
                if (categories.contains(category)) {
                    logger.error(CATEGORY_ALREADY_DEFINED_MSG, category, config.getConfigPid());
                    return false;
                }
            }
        }
        // If we are here then it means all went well above, return true.
        return true;
    }

    private void reconfigureOSGiLoggers() {
        Logger logger = this.loggerContext.getLogger(this.getClass());
        for (LoggerConfig config : this.configByPid.values()) {
            Set<String> categories = config.getCategories();
            String level = config.getLevel();
            for (String category : categories) {
                this.addLogger(category, level, config.isAdditivity());
            }
            logger.info(OSGI_LOGGERS_RECONFIGURED_MSG, categories, level);
        }
    }

    void addServerConfigLoggers(Config loggingCfg) {
        for (Config config : loggingCfg.getConfigList(KEY_LOGGERS)) {
            this.addLogger(config.getString(KEY_LOGGER_NAME).trim(), config.getString(KEY_LOGGER_LEVEL).trim(), config.getBoolean(KEY_LOGGER_ADDITIVITY));
        }
    }

    private void addLogger(String name, String level, boolean additivity) {
        Logger logger = this.loggerContext.getLogger(name);
        logger.setLevel(Level.toLevel(level));
        logger.setAdditive(additivity);
        logger.addAppender(this.consoleAppender);
        logger.addAppender(this.fileAppender);
    }

    void configureRootLogger(Config loggingCfg) {
        Logger root = this.loggerContext.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(loggingCfg.getString(KEY_ROOT_LOG_LEVEL)));
        root.addAppender(this.consoleAppender);
        root.addAppender(this.fileAppender);
    }

    void initConsoleAppender(Config loggingCfg) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(this.loggerContext);
        consoleAppender.setName(loggingCfg.getString(KEY_CONSOLE_APPENDER_NAME));
        consoleAppender.setEncoder(this.newLayoutEncoder(loggingCfg.getString(KEY_LOG_PATTERN_CONSOLE)));
        // we assume Jansi lib is on classpath, use the Jansi maven profile while building runtime.
        if (SystemUtils.IS_OS_WINDOWS) {
            consoleAppender.setWithJansi(true);
        }
        consoleAppender.start();
        this.consoleAppender = consoleAppender;
    }

    void initRollingFileAppender(Config loggingCfg) {
        FileAppenderConfig appenderConfig = this.createFileAppenderConfig(loggingCfg);
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(this.loggerContext);
        fileAppender.setName(appenderConfig.getAppenderName());
        fileAppender.setFile(appenderConfig.getLogFile());
        fileAppender.setAppend(true);
        fileAppender.setEncoder(this.newLayoutEncoder(appenderConfig.getPattern()));
        fileAppender.setImmediateFlush(Boolean.getBoolean(SYS_PROP_LOG_IMMEDIATE_FLUSH));
        if (!fileAppender.isImmediateFlush()) {
            fileAppender.setImmediateFlush(appenderConfig.isImmediateFlush());
        }
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(this.loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(appenderConfig.getLogMaxSize()));
        rollingPolicy.setFileNamePattern(appenderConfig.getRolloverFile());
        rollingPolicy.setMaxHistory(appenderConfig.getLogMaxHistory());
        rollingPolicy.start();
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(rollingPolicy);
        fileAppender.start();
        // Add AsyncAppender support.
        if (appenderConfig.isLogAsync()) {
            this.initAsyncAppender(appenderConfig, fileAppender);
        }
        this.fileAppender = fileAppender;
    }

    private PatternLayoutEncoder newLayoutEncoder(String logPattern) {
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(this.loggerContext);
        layoutEncoder.setPattern(logPattern);
        layoutEncoder.setCharset(UTF_8);
        layoutEncoder.start();
        return layoutEncoder;
    }

    private void initAsyncAppender(FileAppenderConfig rollingFileConfig, FileAppender<ILoggingEvent> fileAppender) {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(this.loggerContext);
        asyncAppender.setName(rollingFileConfig.getAsyncAppenderName());
        asyncAppender.setQueueSize(rollingFileConfig.getAsyncLogQueueSize());
        asyncAppender.setDiscardingThreshold(rollingFileConfig.getAsyncLogDiscardingThreshold());
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();
    }

    private FileAppenderConfig createFileAppenderConfig(Config loggingCfg) {
        return FileAppenderConfig.builder().appenderName(loggingCfg.getString(KEY_FILE_APPENDER_NAME)).logFile(loggingCfg.getString(KEY_SERVER_LOG_FILE)).pattern(loggingCfg.getString(KEY_LOG_PATTERN_FILE)).immediateFlush(loggingCfg.getBoolean(KEY_IMMEDIATE_FLUSH)).logMaxSize(loggingCfg.getString(KEY_LOG_MAX_SIZE)).rolloverFile(loggingCfg.getString(KEY_ROLLOVER_SERVER_LOG_FILE)).logMaxHistory(loggingCfg.getInt(KEY_LOG_MAX_HISTORY)).logAsync(Boolean.getBoolean(SYS_PROP_LOG_ASYNC)).asyncAppenderName(loggingCfg.getString(KEY_ASYNC_APPENDER_NAME)).asyncLogQueueSize(loggingCfg.getInt(KEY_ASYNC_LOG_QUEUE_SIZE)).asyncLogDiscardingThreshold(loggingCfg.getInt(KEY_ASYNC_LOG_DISCARD_THRESHOLD)).build();
    }

    void initLevelChangePropagator() {
        LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(this.loggerContext);
        propagator.setResetJUL(true);
        propagator.start();
        this.loggerContext.addListener(propagator);
    }
}
