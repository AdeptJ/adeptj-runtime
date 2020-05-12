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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * LogbackConfig
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackConfig {

    private String appenderName;

    private String level;

    private String logFile;

    private String rolloverFile;

    private String pattern;

    private int logMaxHistory;

    private String logMaxSize;

    private String logger;

    private boolean additivity;

    private boolean immediateFlush;

    private String asyncAppenderName;

    private int asyncLogQueueSize;

    private int asyncLogDiscardingThreshold;

    private Appender<ILoggingEvent> asyncAppender;

    private LogbackConfig() {
    }

    public String getAppenderName() {
        return appenderName;
    }

    public String getLevel() {
        return level;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getRolloverFile() {
        return rolloverFile;
    }

    public String getPattern() {
        return pattern;
    }

    public int getLogMaxHistory() {
        return logMaxHistory;
    }

    public String getLogMaxSize() {
        return logMaxSize;
    }

    public String getLogger() {
        return logger;
    }

    public boolean isAdditivity() {
        return additivity;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public String getAsyncAppenderName() {
        return asyncAppenderName;
    }

    public int getAsyncLogQueueSize() {
        return asyncLogQueueSize;
    }

    public int getAsyncLogDiscardingThreshold() {
        return asyncLogDiscardingThreshold;
    }

    public Appender<ILoggingEvent> getAsyncAppender() {
        return asyncAppender;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Convenient builder to create {@link LogbackConfig} instances.
     */
    public static class Builder {

        private String appenderName;

        private String level;

        private String logFile;

        private String rolloverFile;

        private String pattern;

        private int logMaxHistory;

        private String logMaxSize;

        private String logger;

        private boolean additivity;

        private boolean immediateFlush;

        private String asyncAppenderName;

        private int asyncLogQueueSize;

        private int asyncLogDiscardingThreshold;

        private Appender<ILoggingEvent> asyncAppender;

        private Builder() {
        }

        public Builder appenderName(String appenderName) {
            this.appenderName = appenderName;
            return this;
        }

        public Builder level(String level) {
            this.level = level;
            return this;
        }

        public Builder logFile(String logFile) {
            this.logFile = logFile;
            return this;
        }

        public Builder rolloverFile(String rolloverFile) {
            this.rolloverFile = rolloverFile;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder logMaxHistory(int logMaxHistory) {
            this.logMaxHistory = logMaxHistory;
            return this;
        }

        public Builder logMaxSize(String logMaxSize) {
            this.logMaxSize = logMaxSize;
            return this;
        }

        public Builder logger(String logger) {
            this.logger = logger;
            return this;
        }

        public Builder additivity(boolean additivity) {
            this.additivity = additivity;
            return this;
        }

        public Builder immediateFlush(boolean immediateFlush) {
            this.immediateFlush = immediateFlush;
            return this;
        }

        public Builder asyncLogQueueSize(int asyncLogQueueSize) {
            this.asyncLogQueueSize = asyncLogQueueSize;
            return this;
        }

        public Builder asyncAppenderName(String asyncAppenderName) {
            this.asyncAppenderName = asyncAppenderName;
            return this;
        }

        public Builder asyncLogDiscardingThreshold(int asyncLogDiscardingThreshold) {
            this.asyncLogDiscardingThreshold = asyncLogDiscardingThreshold;
            return this;
        }

        public Builder asyncAppender(Appender<ILoggingEvent> asyncAppender) {
            this.asyncAppender = asyncAppender;
            return this;
        }

        public LogbackConfig build() {
            LogbackConfig config = new LogbackConfig();
            config.appenderName = this.appenderName;
            config.level = this.level;
            config.logFile = this.logFile;
            config.rolloverFile = this.rolloverFile;
            config.pattern = this.pattern;
            config.logMaxHistory = this.logMaxHistory;
            config.logMaxSize = this.logMaxSize;
            config.logger = this.logger;
            config.additivity = this.additivity;
            config.immediateFlush = this.immediateFlush;
            config.asyncAppenderName = this.asyncAppenderName;
            config.asyncLogQueueSize = this.asyncLogQueueSize;
            config.asyncLogDiscardingThreshold = this.asyncLogDiscardingThreshold;
            config.asyncAppender = this.asyncAppender;
            return config;
        }
    }
}
