/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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

import java.io.IOException;
import java.net.URL;

import static com.adeptj.runtime.common.Constants.EMPTY;
import static com.adeptj.runtime.common.Constants.KEY_BROWSERS;
import static com.adeptj.runtime.common.Constants.MAC_BROWSER_LAUNCH_CMD;
import static com.adeptj.runtime.common.Constants.OS;
import static com.adeptj.runtime.common.Constants.WIN_BROWSER_LAUNCH_CMD;

/**
 * EnvironmentUtils.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class EnvironmentUtils {

    private static final int OFFSET = 0;

    private static final String SPACE = " ";

    private static final String PIPE = " || ";

    private static final String CMD_SH = "sh";

    private static final String CMD_OPT = "-c";

    /**
     * Deny direct instantiation.
     */
    private EnvironmentUtils() {
    }

    public static boolean isMac() {
        return OS.startsWith("Mac");
    }

    public static boolean isWindows() {
        return OS.startsWith("Windows");
    }

    public static boolean isUnix() {
        String os = OS.toLowerCase();
        return os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
    }

    public static void launchBrowser(URL url) throws IOException {
        if (isMac()) {
            Runtime.getRuntime().exec(MAC_BROWSER_LAUNCH_CMD + url);
        } else if (isWindows()) {
            Runtime.getRuntime().exec(WIN_BROWSER_LAUNCH_CMD + url);
        } else if (isUnix()) {
            StringBuilder cmdBuilder = new StringBuilder();
            int index = OFFSET;
            for (String browser : Configs.INSTANCE.common().getStringList(KEY_BROWSERS)) {
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
}
