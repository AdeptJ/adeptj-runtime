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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static com.adeptj.runtime.common.Constants.CURRENT_DIR;
import static com.adeptj.runtime.common.Constants.DIR_ADEPTJ_RUNTIME;
import static com.adeptj.runtime.common.Constants.DIR_DEPLOYMENT;
import static com.adeptj.runtime.common.Constants.EMPTY;
import static com.adeptj.runtime.common.Constants.KEY_BROWSERS;
import static com.adeptj.runtime.common.Constants.MAC_BROWSER_LAUNCH_CMD;
import static com.adeptj.runtime.common.Constants.OS;
import static com.adeptj.runtime.common.Constants.SERVER_CONF_FILE;
import static com.adeptj.runtime.common.Constants.SYS_PROP_SERVER_MODE;
import static com.adeptj.runtime.common.Constants.WIN_BROWSER_LAUNCH_CMD;

/**
 * Environment.
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

    public static boolean isMac() {
        return OS.startsWith("Mac");
    }

    public static boolean isWindows() {
        return OS.startsWith("Windows");
    }

    public static boolean isUnix() {
        return OS.toLowerCase().contains("nix") || OS.toLowerCase().contains("nux");
    }

    public static void launchBrowser(URL url) throws IOException {
        if (isMac()) {
            Runtime.getRuntime().exec(MAC_BROWSER_LAUNCH_CMD + url);
        } else if (isWindows()) {
            Runtime.getRuntime().exec(WIN_BROWSER_LAUNCH_CMD + url);
        } else if (isUnix()) {
            StringBuilder cmdBuilder = new StringBuilder();
            int index = OFFSET;
            for (String browser : Configs.DEFAULT.common().getStringList(KEY_BROWSERS)) {
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

    public static File getServerConfFile() {
        return Paths.get(CURRENT_DIR
                + File.separator
                + DIR_ADEPTJ_RUNTIME
                + File.separator
                + DIR_DEPLOYMENT
                + File.separator
                + SERVER_CONF_FILE).toFile();
    }

    public static boolean isServerConfFileExists() {
        return getServerConfFile().exists();
    }
}
