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

/**
 * Constants, common constants for AdeptJ Runtime.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Constants {

    public static final String BUNDLES_ROOT_DIR_KEY = "BundlesRootDir";

    public static final String BUNDLES_ROOT_DIR_VALUE = "/bundles/";
    
    public static final String BUNDLE_CTX_ATTR = "org.osgi.framework.BundleContext";

    public static final String PROVISIONING_FILE = "provisioning";

    public static final String MAIN_CONF_SECTION = "main";

    public static final String UNDERTOW_CONF_SECTION = "undertow";

    public static final String THYMELEAF_CONF_SECTION = "thymeleaf";

    public static final String TRIMOU_CONF_SECTION = "trimou";

    public static final String FELIX_CONF_SECTION = "felix";

    public static final String COMMON_CONF_SECTION = "common";

    public static final String LOGGING_CONF_SECTION = "logging";

    public static final String CONTEXT_PATH = "/";

    public static final String OSGI_WEBCONSOLE_URI = "/system/console";

    public static final String TOOLS_DASHBOARD_URI = "/tools/dashboard";

    public static final String TOOLS_LOGIN_URI = "/tools/login";

    public static final String TOOLS_LOGOUT_URI = "/tools/logout";

    public static final String BANNER_TXT = "/banner.txt";

    public static final String REGEX_EQ = "=";

    public static final String COMMA = ",";

    public static final String EMPTY = "";

    public static final String UTF8 = "UTF-8";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    public static final String KEY_MAX_CONCURRENT_REQS = "common.max-concurrent-requests";

    public static final String KEY_REQ_BUFF_MAX_BUFFERS = "common.req-buffering-maxBuffers";

    public static final String KEY_ALLOWED_METHODS = "common.server-allowed-methods";

    public static final int MIN_PROCESSORS = 2;

    public static final int IOTHREAD_MULTIPLIER = 8;

    public static final int BUFF_SIZE_1K = 1024;

    public static final int BUFF_SIZE_512_BYTES = 512;

    public static final int DEFAULT_SERVER_PORT = 9007;

    public static final String SYS_PROP_SERVER_MODE = "adeptj.rt.mode";

    public static final String SYS_PROP_SERVER_PORT = "adeptj.rt.port";

    public static final String ARG_OPEN_CONSOLE = "openConsole";

    public static final String DATE_FORMAT_LOGGING = "dd.MM.yyyy HH:mm:ss.SSS ";

    public static final String SHUTDOWN_HOOK_THREAD_NAME = "AdeptJ Terminator";

    public static final String DEPLOYMENT_NAME = "AdeptJ Runtime Deployment";

    public static final String OSGI_CONSOLE_URL = "http://localhost:%s/system/console";

    public static final String WIN_BROWSER_LAUNCH_CMD = "rundll32 url.dll,FileProtocolHandler ";

    public static final String MAC_BROWSER_LAUNCH_CMD = "open ";

    public static final String OS_NAME_PROP = "os.name";

    public static final String OS = System.getProperty(OS_NAME_PROP);

    public static final String CURRENT_DIR = System.getProperty("user.dir");

    public static final String HEADER_X_POWERED_BY = "X-Powered-By";

    public static final String HEADER_SERVER = "Server";

    public static final String PREFIX_ADMIN = "/admin";

    public static final String OSGI_ADMIN_ROLE = "OSGiAdmin";
    
    public static final String MODE_DEV = "DEV";

    public static final String MODE_PROD = "PROD";

    public static final String DIR_ADEPTJ_RUNTIME = "adeptj-runtime";

    public static final String DIR_DEPLOYMENT = "deployment";

    public static final String SERVER_CONF_FILE = "server.conf";

    // Configuration Keys constants start.

    public static final String KEY_BROWSERS = "browsers";

    public static final String KEY_HOST = "host";

    public static final String KEY_PORT = "port";

    public static final String KEY_HTTP = "http";

    public static final String KEY_UT_BUFFER_SIZE = "buffer-size";

    public static final String KEY_UT_DIRECT_BUFFERS = "direct-buffers";

    public static final String KEY_UT_MAX_MEM_128M = "max-mem-128m";

    public static final String KEY_UT_MAX_MEM_64M = "max-mem-64m";

    public static final String KEY_UT_IOTHREADS = "io-threads";

    public static final String KEY_UT_WORKER_THREADS = "worker-threads";

    public static final String KEY_UT_INTERNALS = "internals";

    public static final String KEY_HEADER_POWERED_BY = "common.header-x-powered-by";

    public static final String KEY_HEADER_SERVER = "common.header-server";

    // Configuration Keys constants ends.

    /**
     * Deny direct instantiation.
     */
    private Constants() {
    }
}
