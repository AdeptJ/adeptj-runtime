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

package com.adeptj.runtime.common;

import com.adeptj.runtime.config.Configs;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.adeptj.runtime.common.Constants.DIR_ADEPTJ_RUNTIME;
import static com.adeptj.runtime.common.Constants.DIR_DEPLOYMENT;
import static com.adeptj.runtime.common.Constants.EMPTY;
import static com.adeptj.runtime.common.Constants.FRAMEWORK_CONF_FILE;
import static com.adeptj.runtime.common.Constants.KEY_BROWSERS;
import static com.adeptj.runtime.common.Constants.MAC_BROWSER_LAUNCH_CMD;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.SYS_PROP_SERVER_MODE;
import static com.adeptj.runtime.common.Constants.WIN_BROWSER_LAUNCH_CMD;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

/**
 * Utility methods for getting environment details AdeptJ Runtime is running in.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class Environment {

    private static final int OFFSET = 0;

    private static final String SPACE = " ";

    private static final String PIPE = " || ";

    private static final String CMD_SH = "sh";

    private static final String CMD_OPT = "-c";

    /**
     * Deny direct instantiation.
     */
    private Environment() {
    }

    public static boolean isProd() {
        return ServerMode.PROD.toString().equalsIgnoreCase(System.getProperty(SYS_PROP_SERVER_MODE));
    }

    public static boolean isDev() {
        return ServerMode.DEV.toString().equalsIgnoreCase(System.getProperty(SYS_PROP_SERVER_MODE));
    }

    public static void launchBrowser(URL url) throws IOException {
        if (SystemUtils.IS_OS_MAC) {
            Runtime.getRuntime().exec(MAC_BROWSER_LAUNCH_CMD + url);
        } else if (SystemUtils.IS_OS_WINDOWS) {
            Runtime.getRuntime().exec(WIN_BROWSER_LAUNCH_CMD + url);
        } else if (SystemUtils.IS_OS_UNIX) {
            StringBuilder cmdBuilder = new StringBuilder();
            int index = OFFSET;
            for (String browser : Configs.of().common().getStringList(KEY_BROWSERS)) {
                if (index == OFFSET) {
                    cmdBuilder.append(EMPTY).append(browser).append(SPACE).append(url);
                } else {
                    cmdBuilder.append(PIPE).append(browser).append(SPACE).append(url);
                }
                index++;
            }
            Runtime.getRuntime().exec(new String[]{CMD_SH, CMD_OPT, cmdBuilder.toString()});
        }
    }

    public static Path getServerConfFile() {
        return Paths.get(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, SERVER_CONF_FILE);
    }

    public static boolean isServerConfFileExists() {
        return getServerConfFile().toFile().exists();
    }

    public static Path getFrameworkConfPath() {
        return Paths.get(USER_DIR, DIR_ADEPTJ_RUNTIME, DIR_DEPLOYMENT, FRAMEWORK_CONF_FILE);
    }

    public static boolean isFrameworkConfFileExists() {
        return getFrameworkConfPath().toFile().exists();
    }
}
