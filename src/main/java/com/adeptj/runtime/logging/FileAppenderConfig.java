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

/**
 * Configurations for creating a RollingFileAppender.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class FileAppenderConfig {

    private String appenderName;

    private String logFile;

    private String rolloverFile;

    private String pattern;

    private int logMaxHistory;

    private String logMaxSize;

    private boolean immediateFlush;

    private boolean logAsync;

    private String asyncAppenderName;

    private int asyncLogQueueSize;

    private int asyncLogDiscardingThreshold;

    private FileAppenderConfig() {
    }

    public String getAppenderName() {
        return appenderName;
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

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public boolean isLogAsync() {
        return logAsync;
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

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Convenient builder to create {@link FileAppenderConfig} instances.
     */
    static class Builder {

        private String appenderName;

        private String logFile;

        private String rolloverFile;

        private String pattern;

        private int logMaxHistory;

        private String logMaxSize;

        private boolean immediateFlush;

        private boolean logAsync;

        private String asyncAppenderName;

        private int asyncLogQueueSize;

        private int asyncLogDiscardingThreshold;

        private Builder() {
        }

        public Builder appenderName(String appenderName) {
            this.appenderName = appenderName;
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

        public Builder immediateFlush(boolean immediateFlush) {
            this.immediateFlush = immediateFlush;
            return this;
        }

        public Builder logAsync(boolean logAsync) {
            this.logAsync = logAsync;
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

        public FileAppenderConfig build() {
            FileAppenderConfig config = new FileAppenderConfig();
            config.appenderName = this.appenderName;
            config.logFile = this.logFile;
            config.rolloverFile = this.rolloverFile;
            config.pattern = this.pattern;
            config.logMaxHistory = this.logMaxHistory;
            config.logMaxSize = this.logMaxSize;
            config.immediateFlush = this.immediateFlush;
            config.logAsync = this.logAsync;
            config.asyncAppenderName = this.asyncAppenderName;
            config.asyncLogQueueSize = this.asyncLogQueueSize;
            config.asyncLogDiscardingThreshold = this.asyncLogDiscardingThreshold;
            return config;
        }
    }
}
