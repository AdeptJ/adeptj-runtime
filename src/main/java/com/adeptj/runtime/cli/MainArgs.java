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
package com.adeptj.runtime.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import java.io.File;

/**
 * Program arguments holder.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class MainArgs {

    // Integer types
    @Parameter(names = "-port", description = "Server Http Port")
    public int port;

    @Parameter(names = "-httpsPort", description = "Server Https Port")
    public int httpsPort;

    @Parameter(names = "-shutdownWaitTime", description = "Server shutdown wait time")
    public int shutdownWaitTime;

    @Parameter(names = "-sessionTimeout", description = "Server session timeout in seconds")
    public int sessionTimeout;

    @Parameter(names = "-maxConcurrentRequests", description = "Max concurrent requests server can handle")
    public int maxConcurrentRequests;

    @Parameter(names = "-requestBufferingMaxBuffer", description = "Max buffer size for request buffering")
    public int requestBufferingMaxBuffer;

    @Parameter(names = "-felixLogLevel", description = "Felix log level")
    public int felixLogLevel;

    // Boolean types
    @Parameter(names = "-dev", description = "Server in 'development' mode")
    public boolean dev;

    @Parameter(names = "-scanStartupAwareClasses", description = "Scan StartupAware classes using ServiceLoader")
    public boolean scanStartupAwareClasses;

    @Parameter(names = "-logAsync", description = "Use Logback AsyncFileAppender")
    public boolean logAsync;

    @Parameter(names = "-logImmediateFlush", description = "AsyncFileAppender immediate flush")
    public boolean logImmediateFlush;

    @Parameter(names = "-forceProvisionBundles", description = "Force provision the OSGi bundles from launcher jar")
    public boolean forceProvisionBundles;

    @Parameter(names = "-requestBuffering", description = "Undertow request buffering")
    public boolean requestBuffering;

    @Parameter(names = "-overwriteServerConf", description = "Overwrite server.conf")
    private boolean overwriteServerConf;

    @Parameter(names = "-overwriteFrameworkConf", description = "Overwrite framework.properties")
    private boolean overwriteFrameworkConf;

    @Parameter(names = "-logFrameworkError", description = "Log OSGi framework error using the FrameworkListener")
    private boolean logFrameworkError;

    // String types
    @Parameter(names = "-tls", description = "TLS version")
    public String tlsVersion;

    @Parameter(names = "-p12Password", description = "p12 password")
    public String p12Password;

    // File types
    @Parameter(names = "-p12File",
            converter = FileConverter.class, description = "Absolute path of the p12 file")
    public File p12File;
}
