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

import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import io.undertow.Undertow;

/**
 * UNDERTOW Server Options.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ServerOptions extends BaseOptions {

    private static final String SERVER_OPTIONS = "server-options";

    private static final String OPTIONS_TYPE_OTHERS = "options-type-others";

    private static final String OPTIONS_TYPE_LONG = "options-type-long";

    /**
     * Configures the server options dynamically.
     *
     * @param builder        Undertow.Builder
     * @param undertowConfig Undertow Typesafe Config
     */
    @Override
    public void setOptions(Undertow.Builder builder, Config undertowConfig) {
        long startTime = System.nanoTime();
        Config serverOptionsCfg = undertowConfig.getConfig(SERVER_OPTIONS);
        serverOptionsCfg.getObject(OPTIONS_TYPE_OTHERS)
                .unwrapped()
                .forEach((key, val) -> builder.setServerOption(this.getOption(key), val));
        serverOptionsCfg.getObject(OPTIONS_TYPE_LONG)
                .unwrapped()
                .forEach((key, val) -> builder.setServerOption(this.getOption(key), Long.valueOf((Integer) val)));
        this.logger.info("Undertow ServerOptions configured in [{}] ms!!", Times.elapsedMillis(startTime));
    }
}
