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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.adeptj.runtime.common.Constants.EMPTY;
import static com.adeptj.runtime.common.Constants.KEY_BROWSERS;
import static com.adeptj.runtime.common.Constants.MAC_BROWSER_LAUNCH_CMD;
import static com.adeptj.runtime.common.Constants.OS;
import static com.adeptj.runtime.common.Constants.WIN_BROWSER_LAUNCH_CMD;

/**
 * Common Utilities
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class Utils {

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private static final String SPACE = " ";

    private static final String PIPE = " || ";

    private static final String CMD_SH = "sh";

    private static final String CMD_OPT = "-c";

    private static final int OFFSET = 0;

    /**
     * Deny direct instantiation.
     */
    private Utils() {
    }

    public static String toString(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toString(Constants.UTF8);
    }

    public static byte[] toBytes(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toByteArray();
    }

    public static ByteArrayOutputStream toByteArrayOutputStream(InputStream input) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numberOfBytesRead;
        while ((numberOfBytesRead = input.read(buffer)) != EOF) {
            out.write(buffer, OFFSET, numberOfBytesRead);
        }
        return out;
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
