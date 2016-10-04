/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
 */
package com.adeptj.modularweb.micro.common;

import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.config.Configs;
import com.typesafe.config.Config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 * This Class initializes the Logback logging framework.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackInitializer {
	
	public static void init() {

		Config commonConf = Configs.INSTANCE.main().getConfig("common");
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		String logPattern = commonConf.getString("log-pattern");

		// Pattern Layout For ConsoleAppender
		PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
		consoleEncoder.setContext(context);
		consoleEncoder.setPattern(logPattern);
		consoleEncoder.start();

		// Pattern Layout For FileAppender
		PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
		fileEncoder.setContext(context);
		fileEncoder.setPattern(logPattern);
		fileEncoder.start();

		// Console Appender
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setName("CONSOLE");
		consoleAppender.setContext(context);
		consoleAppender.setEncoder(consoleEncoder);
		consoleAppender.start();

		String logFile = commonConf.getString("startup-log-file");

		// File Appender
		RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
		fileAppender.setFile(logFile + ".log");
		fileAppender.setAppend(true);
		fileAppender.setEncoder(fileEncoder);
		fileAppender.setName("FILE");
		fileAppender.setContext(context);

		// Rolling Policy
		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(context);
		rollingPolicy.setFileNamePattern(logFile + "-" + commonConf.getString("log-rolling-pattern"));
		rollingPolicy.setMaxHistory(commonConf.getInt("log-max-history"));
		rollingPolicy.setParent(fileAppender);

		// Triggering Policy
		SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = new SizeAndTimeBasedFNATP<>();
		triggeringPolicy.setMaxFileSize(commonConf.getString("log-max-size"));

		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
		rollingPolicy.start();

		// Set Rolling and Triggering Policy to RollingFileAppender
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.setTriggeringPolicy(triggeringPolicy);
		fileAppender.start();

		// Set root at ERROR level.
		Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.ERROR);
		rootLogger.addAppender(consoleAppender);

		Logger adeptjLogger = context.getLogger("com.adeptj");
		adeptjLogger.setLevel(Level.INFO);
		adeptjLogger.addAppender(fileAppender);
		adeptjLogger.addAppender(consoleAppender);
		adeptjLogger.setAdditive(false);

		Logger undertowLogger = context.getLogger("io.undertow");
		undertowLogger.setLevel(Level.ERROR);
		undertowLogger.addAppender(fileAppender);
		undertowLogger.addAppender(consoleAppender);
		undertowLogger.setAdditive(false);

		Logger xnioLogger = context.getLogger("org.xnio.nio");
		xnioLogger.setLevel(Level.ERROR);
		xnioLogger.addAppender(fileAppender);
		xnioLogger.addAppender(consoleAppender);
		xnioLogger.setAdditive(false);

		Logger undertowSecurityLogger = context.getLogger("io.undertow.request.security");
		undertowSecurityLogger.setLevel(Level.ERROR);
		undertowSecurityLogger.addAppender(consoleAppender);
		undertowSecurityLogger.addAppender(fileAppender);
		undertowSecurityLogger.setAdditive(false);
	}
}
