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

package com.adeptj.runtime.undertow.core;

import com.typesafe.config.Config;
import io.undertow.Undertow;
import org.xnio.Option;

/**
 * Undertow Worker Options.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class WorkerOptions extends BaseOptions {

    private static final String WORKER_OPTIONS = "worker-options";

    /**
     * Configures the worker options dynamically.
     *
     * @param builder        Undertow.Builder
     * @param undertowConfig Undertow Typesafe Config
     */
    @Override
    public void setOptions(Undertow.Builder builder, Config undertowConfig) {
        undertowConfig.getObject(WORKER_OPTIONS)
                .unwrapped()
                .forEach((key, val) -> builder.setWorkerOption(this.getOption(key), val));
    }

    public <T> WorkerOptions overrideOption(Undertow.Builder builder, Option<T> option, T value) {
        builder.setWorkerOption(option, value);
        return this;
    }
}
