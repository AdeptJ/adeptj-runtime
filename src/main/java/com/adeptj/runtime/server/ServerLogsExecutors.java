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

package com.adeptj.runtime.server;

import com.adeptj.runtime.config.Configs;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Provides {@link ExecutorService} with fixed thread pool for tailing server logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
enum ServerLogsExecutors {

    INSTANCE;

    private static final String WS_OPTIONS = "webSocket-options";

    private static final String THREAD_POOL_SIZE = "thread-pool-size";

    private static int executionCount;

    private ExecutorService executorService;

    private int poolSize;

    ServerLogsExecutors() {
        this.poolSize = Configs.DEFAULT.undertow().getConfig(WS_OPTIONS).getInt(THREAD_POOL_SIZE);
    }

    void execute(Runnable command) {
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(this.poolSize);
            executionCount = this.poolSize;
        }
        if (executionCount == 0) {
            throw new RejectedExecutionException("Can't accept more server logs requests!!");
        }
        this.executorService.execute(command);
        executionCount--;
    }

    void shutdownExecutorService() {
        Optional.ofNullable(this.executorService).ifPresent(ExecutorService::shutdown);
    }
}
