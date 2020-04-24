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

import com.adeptj.runtime.common.Times;
import com.typesafe.config.Config;
import io.undertow.Undertow.Builder;

import java.util.Map;

/**
 * Undertow Socket Options.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class SocketOptions extends BaseOptions {

    private static final String SOCKET_OPTIONS = "socket-options";

    private static final String OPTIONS_TYPE_INTEGER = "options-type-integer";

    private static final String OPTIONS_TYPE_BOOLEAN = "options-type-boolean";

    /**
     * Configures the socket options dynamically.
     *
     * @param builder        Undertow.Builder
     * @param undertowConfig Undertow Typesafe Config
     */
    public Builder build(Builder builder, Config undertowConfig) {
        long startTime = System.nanoTime();
        Config socketOptionsCfg = undertowConfig.getConfig(SOCKET_OPTIONS);
        integerOptions(builder, socketOptionsCfg.getObject(OPTIONS_TYPE_INTEGER).unwrapped());
        booleanOptions(builder, socketOptionsCfg.getObject(OPTIONS_TYPE_BOOLEAN).unwrapped());
        this.logger.info("Undertow SocketOptions set in [{}] ms!!", Times.elapsedMillis(startTime));
        return builder;
    }

    private void buildSocketOptions(Builder builder, Map<String, ?> options) {
        options.forEach((optKey, optVal) -> builder.setSocketOption(toOption(optKey), optVal));
    }

    private void integerOptions(Builder builder, Map<String, ?> options) {
        buildSocketOptions(builder, options);
    }

    private void booleanOptions(Builder builder, Map<String, ?> options) {
        buildSocketOptions(builder, options);
    }
}
