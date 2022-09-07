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

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.util.Environment;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

import static com.adeptj.runtime.common.Constants.COMMON_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.FELIX_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.LOGGING_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.RESTEASY_CONF_SECTION;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.SYS_PROP_OVERWRITE_SERVER_CONF;
import static com.adeptj.runtime.common.Constants.TRIMOU_CONF_SECTION;

/**
 * Initializes the application configurations.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Configs {

    INSTANCE;

    /**
     * This will include system properties as well.
     */
    private Config root;

    Configs() {
        // this.root = this.loadConf();
    }

    public Config root() {
        return this.root;
    }

    public Config main() {
        return ConfigProvider.getInstance().getMainConfig();
    }

    public Config undertow(ServerRuntime runtime) {
        return ConfigProvider.getInstance().getServerConfig(runtime);
    }

    public Config felix() {
        return this.main().getConfig(FELIX_CONF_SECTION);
    }

    public Config common() {
        return this.main().getConfig(COMMON_CONF_SECTION);
    }

    public Config logging() {
        return this.main().getConfig(LOGGING_CONF_SECTION);
    }

    public Config trimou() {
        return this.main().getConfig(TRIMOU_CONF_SECTION);
    }

    public Config resteasy() {
        return this.main().getConfig(RESTEASY_CONF_SECTION);
    }

    private Config loadConf() {
        Config config;
        File configFile = Environment.getServerConfPath().toFile();
        if (configFile.exists()) {
            // if overwrite.server.conf.file system property is provided, then load the configs from classpath.
            if (Boolean.getBoolean(SYS_PROP_OVERWRITE_SERVER_CONF)) {
                config = ConfigFactory.load(SERVER_CONF_FILE);
            } else {
                config = ConfigFactory.parseFile(configFile)
                        .withFallback(ConfigFactory.systemProperties())
                        .resolve();
            }
        } else {
            config = ConfigFactory.load(SERVER_CONF_FILE);
        }
        return config;
    }

    public static Configs of() {
        return INSTANCE;
    }

}
