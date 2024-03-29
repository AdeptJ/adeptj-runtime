/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel.util;

import com.adeptj.runtime.kernel.ServerMode;

import java.nio.file.Path;

import static com.adeptj.runtime.kernel.Constants.CONF_DIR;
import static com.adeptj.runtime.kernel.Constants.DIR_ADEPTJ_RUNTIME;
import static com.adeptj.runtime.kernel.Constants.DIR_DEPLOYMENT;
import static com.adeptj.runtime.kernel.Constants.FRAMEWORK_CONF_FILE;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_MODE;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

/**
 * Utility methods for getting environment details AdeptJ Runtime is running in.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class Environment {

    /**
     * Deny direct instantiation.
     */
    private Environment() {
    }

    public static boolean isDev() {
        return ServerMode.DEV.toString().equalsIgnoreCase(System.getProperty(SYS_PROP_SERVER_MODE));
    }

    public static Path getConfDirPath() {
        return Path.of(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, CONF_DIR);
    }

    public static Path getFrameworkConfPath() {
        return Path.of(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, FRAMEWORK_CONF_FILE);
    }
}
