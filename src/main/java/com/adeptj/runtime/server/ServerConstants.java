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

    static final String KEY_IGNORE_FLUSH = "common.ignore-flush";

    static final int SYS_TASK_THREAD_MULTIPLIER = 2;

    static final int WORKER_TASK_THREAD_MULTIPLIER = 8;

    static final String KEY_WORKER_TASK_MAX_THREADS = "worker-task-max-threads";

    static final String KEY_WORKER_TASK_CORE_THREADS = "worker-task-core-threads";

    static final String KEY_TCP_NO_DELAY = "tcp-no-delay";

    static final String KEY_WORKER_OPTIONS = "worker-options";

    static final String KEY_KEYSTORE = "keyStore";

    static final String KEY_HTTPS = "https";

    static final String SYS_PROP_ENABLE_HTTP2 = "enable.http2";

    static final String SYS_PROP_SERVER_HTTPS_PORT = "adeptj.rt.https.port";

    static final String SYS_PROP_CHECK_PORT = "adeptj.rt.port.check";

    static final String KEY_DEFAULT_ENCODING = "common.default-encoding";

    static final String REALM = "AdeptJ Realm";

    static final String SYS_PROP_SESSION_TIMEOUT = "adeptj.session.timeout";

    static final String SYS_PROP_ENABLE_REQ_BUFF = "enable.req.buffering";

    static final String SYS_PROP_REQ_BUFF_MAX_BUFFERS = "req.buff.maxBuffers";

    static final String SYS_PROP_MAX_CONCUR_REQ = "max.concurrent.requests";

    static final String SYS_PROP_WORKER_TASK_THREAD_MULTIPLIER = "worker.task.thread.multiplier";

    static final String SYS_PROP_SYS_TASK_THREAD_MULTIPLIER = "sys.task.thread.multiplier";

    static final String KEY_SESSION_TIMEOUT = "common.session-timeout";

    static final String KEY_HTTP_ONLY = "common.session-cookie-httpOnly";

    static final String KEY_CHANGE_SESSIONID_ON_LOGIN = "common.change-sessionId-on-login";

    static final String KEY_INVALIDATE_SESSION_ON_LOGOUT = "common.invalidate-session-on-logout";

    static final String KEY_USE_CACHED_AUTH_MECHANISM = "common.use-cached-auth-mechanism";

    static final String KEY_WS_IO_THREADS = "io-threads";

    static final String KEY_WS_TASK_CORE_THREADS = "task-core-threads";

    static final String KEY_WS_TASK_MAX_THREADS = "task-max-threads";

    static final String KEY_WS_TCP_NO_DELAY = "tcp-no-delay";

    static final String KEY_WS_WEB_SOCKET_OPTIONS = "webSocket-options";

    static final String KEY_WS_USE_DIRECT_BUFFER = "use-direct-buffer";

    static final String KEY_WS_BUFFER_SIZE = "buffer-size";

    static final String KEY_MULTIPART_FILE_LOCATION = "common.multipart-file-location";

    static final String KEY_MULTIPART_MAX_FILE_SIZE = "common.multipart-max-file-size";

    static final String KEY_MULTIPART_MAX_REQUEST_SIZE = "common.multipart-max-request-size";

    static final String KEY_MULTIPART_FILE_SIZE_THRESHOLD = "common.multipart-file-size-threshold";

    static final String KEY_AUTH_ROLES = "common.auth-roles";

    static final String KEY_SECURED_URLS_ALLOWED_METHODS = "common.secured-urls-allowed-methods";

    static final String KEY_SECURED_URLS = "common.secured-urls";

    static final String KEY_ERROR_PAGES = "error-pages";

    static final String KEY_USER_CREDENTIAL_MAPPING = "common.user-credential-mapping";

    static final int PWD_START_INDEX = 9;

    static final long DEFAULT_WAIT_TIME = 30000L;

    static final String SYS_PROP_SHUTDOWN_WAIT_TIME = "shutdown.wait.time";

    static final String ERROR_SERVLET_NAME = "AdeptJ ErrorServlet";

    static final String ERROR_URI_401 = "/error/401";

    static final String ERROR_URI_403 = "/error/403";

    static final String ERROR_URI_404 = "/error/404";

    static final String ERROR_URI_500 = "/error/500";

    static final String ADMIN_SERVLET_NAME = "AdeptJ AdminServlet";

    static final String CRYPTO_SERVLET_NAME = "AdeptJ CryptoServlet";
}
