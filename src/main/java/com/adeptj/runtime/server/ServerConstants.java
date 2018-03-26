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

package com.adeptj.runtime.server;

/**
 * Constants for AdeptJ Runtime {@link Server}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class ServerConstants {

    private ServerConstants() {
    }

    final static String KEY_IGNORE_FLUSH = "common.ignore-flush";

    final static int SYS_TASK_THREAD_MULTIPLIER = 2;

    final static int WORKER_TASK_THREAD_MULTIPLIER = 8;

    final static String KEY_WORKER_TASK_MAX_THREADS = "worker-task-max-threads";

    final static String KEY_WORKER_TASK_CORE_THREADS = "worker-task-core-threads";

    final static String KEY_WORKER_OPTIONS = "worker-options";

    final static String KEY_KEYSTORE = "keyStore";

    final static String KEY_HTTPS = "https";

    final static String SYS_PROP_ENABLE_HTTP2 = "enable.http2";

    final static String KEY_AJP = "ajp";

    final static String SYS_PROP_ENABLE_AJP = "enable.ajp";

    final static String SYS_PROP_CHECK_PORT = "adeptj.rt.port.check";

    final static String KEY_DEFAULT_ENCODING = "common.default-encoding";

    final static String REALM = "AdeptJ Realm";

    final static String SYS_PROP_SESSION_TIMEOUT = "adeptj.session.timeout";

    final static String SYS_PROP_ENABLE_REQ_BUFF = "enable.req.buffering";

    final static String SYS_PROP_REQ_BUFF_MAX_BUFFERS = "req.buff.maxBuffers";

    final static String SYS_PROP_MAX_CONCUR_REQ = "max.concurrent.requests";

    final static String KEY_SESSION_TIMEOUT = "common.session-timeout";

    final static String KEY_HTTP_ONLY = "common.session-cookie-httpOnly";

    final static String KEY_CHANGE_SESSIONID_ON_LOGIN = "common.change-sessionId-on-login";

    final static String KEY_INVALIDATE_SESSION_ON_LOGOUT = "common.invalidate-session-on-logout";

    final static String KEY_USE_CACHED_AUTH_MECHANISM = "common.use-cached-auth-mechanism";

    final static String KEY_WS_IO_THREADS = "io-threads";

    final static String KEY_WS_TASK_CORE_THREADS = "task-core-threads";

    final static String KEY_WS_TASK_MAX_THREADS = "task-max-threads";

    final static String KEY_WS_TCP_NO_DELAY = "tcp-no-delay";

    final static String KEY_WS_WEB_SOCKET_OPTIONS = "webSocket-options";

    final static String KEY_WS_USE_DIRECT_BUFFER = "use-direct-buffer";

    final static String KEY_WS_BUFFER_SIZE = "buffer-size";

    final static String KEY_MULTIPART_FILE_LOCATION = "common.multipart-file-location";

    final static String KEY_MULTIPART_MAX_FILE_SIZE = "common.multipart-max-file-size";

    final static String KEY_MULTIPART_MAX_REQUEST_SIZE = "common.multipart-max-request-size";

    final static String KEY_MULTIPART_FILE_SIZE_THRESHOLD = "common.multipart-file-size-threshold";

    final static String KEY_AUTH_ROLES = "common.auth-roles";

    final static String KEY_SECURED_URLS_ALLOWED_METHODS = "common.secured-urls-allowed-methods";

    final static String KEY_SECURED_URLS = "common.secured-urls";

    final static String KEY_ERROR_PAGES = "error-pages";

    final static long DEFAULT_WAIT_TIME = 60000L;

    final static String SYS_PROP_SHUTDOWN_WAIT_TIME = "shutdown.wait.time";

    final static String ERROR_PAGE_SERVLET = "AdeptJ ErrorPageServlet";

    final static String TOOLS_ERROR_URL = "/tools/error/*";

    final static String TOOLS_SERVLET = "AdeptJ ToolsServlet";

    final static String TOOLS_DASHBOARD_URL = "/tools/dashboard";

    final static String AUTH_SERVLET = "AdeptJ AuthServlet";

    final static String CRYPTO_SERVLET = "AdeptJ CryptoServlet";
}
