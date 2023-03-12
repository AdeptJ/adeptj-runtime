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

package com.adeptj.runtime.kernel;

/**
 * Constants, common constants for AdeptJ Runtime.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Constants {

    public static final String SHA_256 = "SHA-256";

    public static final String ADMIN_LOGIN_URI = "/admin/login";

    public static final String KEY_MAX_CONCURRENT_REQUESTS = "common.max-concurrent-requests";

    public static final String KEY_REQ_LIMIT_QUEUE_SIZE = "common.req-limit-queue-size";

    public static final String KEY_REQ_BUFF_MAX_BUFFERS = "common.req-buffering-maxBuffers";

    public static final String KEY_ALLOWED_METHODS = "common.server-allowed-methods";

    public static final String KEY_ERROR_HANDLER_CODES = "common.error-handler-codes";

    public static final String KEY_ERROR_HANDLER_PATH = "common.error-handler-path";

    public static final String SYS_PROP_SERVER_PORT = "adeptj.rt.port";

    public static final String DEPLOYMENT_NAME = "AdeptJ Runtime Deployment";

    public static final String DIR_ADEPTJ_RUNTIME = "adeptj-runtime";

    public static final String DIR_DEPLOYMENT = "deployment";

    public static final String CONF_DIR = "conf";

    public static final String KEY_HOST = "host";

    public static final String KEY_PORT = "port";

    public static final String KEY_HTTP = "http";

    public static final String MV_CREDENTIALS_STORE = "credentials.dat";

    public static final String H2_MAP_ADMIN_CREDENTIALS = "adminCredentials";

    public static final String KEY_SYSTEM_CONSOLE_PATH = "common.system-console-path";

    public static final String FRAMEWORK_CONF_FILE = "framework.properties";

    public static final String SYS_PROP_SERVER_MODE = "adeptj.rt.mode";

    public static final String KEY_P12_FILE_LOCATION = "p12-file-location";

    public static final String KEY_P12_PASSWORD = "p12-password";

    public static final String SYS_PROP_P12_FILE_LOCATION = "adeptj.rt.p12-file-location";

    public static final String SYS_PROP_P12_PASSWORD = "adeptj.rt.p12-password";

    public static final String SYS_PROP_P12_FILE_EXTERNAL = "adeptj.rt.p12-file-external";

    public static final String SYS_PROP_TLS_VERSION = "tls.version";

    public static final String KEY_TLS_VERSION = "tlsVersion";

    public static final String KEY_KEYSTORE_TYPE = "keystore-type";

    public static final String SYS_PROP_OVERWRITE_CONF_FILES = "overwrite.conf.files";

    /**
     * Deny direct instantiation.
     */
    private Constants() {
    }
}
