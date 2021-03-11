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

import com.beust.jcommander.Parameter;

/**
 * Program arguments holder.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class MainArgs {

    @Parameter(names = "-debug", description = "Debug mode")
    public boolean debug;

    @Parameter(names = "-waitTimeForDebugAttach", description = "Time to wait for debug attach")
    public int waitTimeForDebugAttach = 5;

    @Parameter(names = "-httpPort", description = "Server Http Port")
    public int httpPort;

    @Parameter(names = "-httpsPort", description = "Server Https Port")
    public int httpsPort;

    @Parameter(names = "-enableHttp2", description = "Enable Http2")
    public boolean enableHttp2;

    private static final MainArgs INSTANCE = new MainArgs();

    public static MainArgs getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "MainArgs{" +
                "debug=" + debug +
                ", waitTimeFormDebugAttach='" + waitTimeForDebugAttach + '\'' +
                ", httpPort=" + httpPort +
                ", httpsPort=" + httpsPort +
                ", enableHttp2=" + enableHttp2 +
                '}';
    }
}
