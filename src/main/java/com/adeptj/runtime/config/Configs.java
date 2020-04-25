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

package com.adeptj.runtime.config;

import com.adeptj.runtime.common.Environment;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.nio.file.Path;

import static com.adeptj.runtime.common.Constants.COMMON_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.FELIX_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.LOGGING_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.MAIN_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.TRIMOU_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.UNDERTOW_CONF_SECTION;

/**
 * Initializes the application configurations.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Configs {

    INSTANCE;

    private final Config main;

    Configs() {
        this.main = this.loadConf();
    }

    public Config main() {
        return this.main;
    }

    public Config undertow() {
        return this.main.getConfig(UNDERTOW_CONF_SECTION);
    }

    public Config felix() {
        return this.main.getConfig(FELIX_CONF_SECTION);
    }

    public Config common() {
        return this.main.getConfig(COMMON_CONF_SECTION);
    }

    public Config logging() {
        return this.main.getConfig(LOGGING_CONF_SECTION);
    }

    public Config trimou() {
        return this.main.getConfig(TRIMOU_CONF_SECTION);
    }

    private Config loadConf() {
        Path configFile = Environment.getServerConfFile();
        if (configFile.toFile().exists()) {
            return ConfigFactory.parseFile(configFile.toFile())
                    .withFallback(ConfigFactory.systemProperties())
                    .resolve()
                    .getConfig(MAIN_CONF_SECTION);
        } else {
            return ConfigFactory.load(SERVER_CONF_FILE).getConfig(MAIN_CONF_SECTION);
        }
    }

    public static Configs of() {
        return INSTANCE;
    }

}
