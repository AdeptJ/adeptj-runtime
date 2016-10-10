/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, Rakesh Kumar, AdeptJ (http://adeptj.com)                 #
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
package com.adeptj.modularweb.micro.common;

import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.config.Configs;
import com.typesafe.config.Config;

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

/**
 * This Class initializes the LOGBACK logging framework. Usually LOGBACK is initialized via logback.xml file on CLASSPATH. 
 * But using that approach LOGBACK takes longer to initializes(5+ seconds) which is reduced drastically to under 150 milliseconds
 * using programmatic approach. This is a great improvement on total startup time.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackInitializer {

	public static final String LOGGER_XNIO = "org.xnio.nio";

	public static final String LOGGER_UNDERTOW = "io.undertow";

	public static final String LOGGER_ADEPTJ = "com.adeptj";

	public static final String KEY_STARTUP_LOG_FILE = "startup-log-file";

	public static final String KEY_LOG_PATTERN = "log-pattern";

	public static final String KEY_LOG_MAX_HISTORY = "log-max-history";

	public static final String KEY_LOG_ROLLING_PATTERN = "log-rolling-pattern";

	public static final String KEY_LOG_MAX_SIZE = "log-max-size";

	public static final String CONF_COMMON = "common";

	public static final String HYPHEN = "-";

	public static final String EXTN_LOG = ".log";

	public static final String APPENDER_CONSOLE = "CONSOLE";

	public static final String APPENDER_FILE = "FILE";

	public static void init() {
		long startTime = System.currentTimeMillis();
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Config commonConf = Configs.INSTANCE.main().getConfig(CONF_COMMON);
		// Console Appender
		ConsoleAppender<ILoggingEvent> consoleAppender = consoleAppender(context, commonConf);
		// File Appender
		RollingFileAppender<ILoggingEvent> fileAppender = fileAppender(context, commonConf);
		// Rolling Policy
		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = rollingPolicy(commonConf, context);
		rollingPolicy.setParent(fileAppender);
		// Triggering Policy
		SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = triggeringPolicy(commonConf);
		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
		rollingPolicy.start();
		// Set Rolling and Triggering Policy to RollingFileAppender
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.setTriggeringPolicy(triggeringPolicy);
		fileAppender.start();
		// initialize all the required loggers.
		rootLogger(context, consoleAppender, commonConf);
		List<Appender<ILoggingEvent>> appenders = Arrays.asList(consoleAppender, fileAppender);
		adeptjLogger(context, appenders, commonConf);
		undertowLogger(context, appenders, commonConf);
		xnioLogger(context, appenders, commonConf);
		context.start();
		context.getLogger(LogbackInitializer.class).info("Logback initialized in [{}] ms!!", (System.currentTimeMillis() - startTime));
	}
	
	public static void destroy() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.stop();
	}
	
	private static void addAppenders(List<Appender<ILoggingEvent>> appenders, Logger logger) {
		for (Appender<ILoggingEvent> appender : appenders) {
			logger.addAppender(appender);
		}
	}

	private static void rootLogger(LoggerContext context, ConsoleAppender<ILoggingEvent> consoleAppender, Config commonConf) {
		// initialize ROOT Logger at ERROR level.
		Logger root = logger(Logger.ROOT_LOGGER_NAME, Level.valueOf(commonConf.getString("root-log-level")), context);
		root.addAppender(consoleAppender);
	}
	
	private static void xnioLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config commonConf) {
		Logger xnioLogger = logger(LOGGER_XNIO, Level.valueOf(commonConf.getString("xnio-log-level")), context);
		addAppenders(appenders, xnioLogger);
		xnioLogger.setAdditive(false);
	}

	private static void undertowLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config commonConf) {
		Logger undertowLogger = logger(LOGGER_UNDERTOW, Level.valueOf(commonConf.getString("undertow-log-level")), context);
		addAppenders(appenders, undertowLogger);
		undertowLogger.setAdditive(false);
	}

	private static void adeptjLogger(LoggerContext context, List<Appender<ILoggingEvent>> appenders, Config commonConf) {
		Logger adeptjLogger = logger(LOGGER_ADEPTJ, Level.valueOf(commonConf.getString("adeptj-log-level")), context);
		addAppenders(appenders, adeptjLogger);
		adeptjLogger.setAdditive(false);
	}

	private static Logger logger(String name, Level level, LoggerContext context) {
		Logger logger = context.getLogger(name);
		logger.setLevel(level);
		return logger;
	}

	private static SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy(Config commonConf) {
		SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = new SizeAndTimeBasedFNATP<>();
		triggeringPolicy.setMaxFileSize(commonConf.getString(KEY_LOG_MAX_SIZE));
		return triggeringPolicy;
	}

	private static TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy(Config commonConf, LoggerContext context) {
		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(context);
		rollingPolicy.setFileNamePattern(
				commonConf.getString(KEY_STARTUP_LOG_FILE) + HYPHEN + commonConf.getString(KEY_LOG_ROLLING_PATTERN));
		rollingPolicy.setMaxHistory(commonConf.getInt(KEY_LOG_MAX_HISTORY));
		return rollingPolicy;
	}

	private static RollingFileAppender<ILoggingEvent> fileAppender(LoggerContext context, Config commonConf) {
		RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
		fileAppender.setFile(commonConf.getString(KEY_STARTUP_LOG_FILE) + EXTN_LOG);
		fileAppender.setAppend(true);
		fileAppender.setEncoder(newLayoutEncoder(context, commonConf.getString(KEY_LOG_PATTERN)));
		fileAppender.setName(APPENDER_FILE);
		fileAppender.setContext(context);
		return fileAppender;
	}

	private static ConsoleAppender<ILoggingEvent> consoleAppender(LoggerContext context, Config commonConf) {
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setName(APPENDER_CONSOLE);
		consoleAppender.setContext(context);
		consoleAppender.setEncoder(newLayoutEncoder(context, commonConf.getString(KEY_LOG_PATTERN)));
		consoleAppender.start();
		return consoleAppender;
	}

	private static PatternLayoutEncoder newLayoutEncoder(LoggerContext context, String logPattern) {
		PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
		layoutEncoder.setContext(context);
		layoutEncoder.setPattern(logPattern);
		layoutEncoder.start();
		return layoutEncoder;
	}
}