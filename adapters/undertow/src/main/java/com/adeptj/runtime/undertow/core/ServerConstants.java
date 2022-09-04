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

package com.adeptj.runtime.undertow.core;

/**
 * Constants for AdeptJ Runtime UndertowServer.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ServerConstants {

    private ServerConstants() {
    }

    public static final String KEY_IGNORE_FLUSH = "common.ignore-flush";

    public static final int SYS_TASK_THREAD_MULTIPLIER = 2;

    public static final int WORKER_TASK_THREAD_MULTIPLIER = 8;

    public static final String KEY_WORKER_TASK_MAX_THREADS = "WORKER_TASK_MAX_THREADS";

    public static final String KEY_WORKER_TASK_CORE_THREADS = "WORKER_TASK_CORE_THREADS";

    public static final String KEY_WORKER_OPTIONS = "worker-options";

    public static final String KEY_HTTPS = "https";

    public static final String SYS_PROP_ENABLE_HTTP2 = "enable.http2";

    public static final String SYS_PROP_SERVER_HTTPS_PORT = "adeptj.rt.https.port";

    public static final String KEY_DEFAULT_ENCODING = "common.default-encoding";

    public static final String REALM = "AdeptJ Realm";

    public static final String SYS_PROP_SESSION_TIMEOUT = "adeptj.session.timeout";

    public static final String SYS_PROP_ENABLE_REQ_BUFF = "enable.req.buffering";

    public static final String SYS_PROP_REQ_BUFF_MAX_BUFFERS = "req.buff.maxBuffers";

    public static final String SYS_PROP_MAX_CONCUR_REQ = "max.concurrent.requests";

    public static final String SYS_PROP_REQ_LIMIT_QUEUE_SIZE = "req.limit.queue.size";

    public static final String SYS_PROP_WORKER_TASK_THREAD_MULTIPLIER = "worker.task.thread.multiplier";

    public static final String SYS_PROP_SYS_TASK_THREAD_MULTIPLIER = "sys.task.thread.multiplier";

    public static final String KEY_SESSION_TIMEOUT = "common.session-timeout";

    public static final String KEY_HTTP_ONLY = "common.session-cookie-httpOnly";

    public static final String KEY_CHANGE_SESSION_ID_ON_LOGIN = "common.change-sessionId-on-login";

    public static final String KEY_INVALIDATE_SESSION_ON_LOGOUT = "common.invalidate-session-on-logout";

    public static final String KEY_USE_CACHED_AUTH_MECHANISM = "common.use-cached-auth-mechanism";

    public static final String KEY_MULTIPART_FILE_LOCATION = "common.multipart-file-location";

    public static final String KEY_MULTIPART_MAX_FILE_SIZE = "common.multipart-max-file-size";

    public static final String KEY_MULTIPART_MAX_REQUEST_SIZE = "common.multipart-max-request-size";

    public static final String KEY_MULTIPART_FILE_SIZE_THRESHOLD = "common.multipart-file-size-threshold";

    public static final String KEY_AUTH_ROLES = "common.auth-roles";

    public static final String KEY_PROTECTED_PATHS = "common.protected-paths";

    public static final String KEY_PROTECTED_PATHS_SECURED_FOR_METHODS = "common.protected-paths-secured-for-methods";

    public static final String KEY_USER_CREDENTIAL_MAPPING = "common.user-credential-mapping";

    public static final int PWD_START_INDEX = 9;

    public static final long DEFAULT_WAIT_TIME = 30000L;

    public static final String SYS_PROP_SHUTDOWN_WAIT_TIME = "shutdown.wait.time";

    public static final String ERROR_SERVLET_NAME = "AdeptJ ErrorServlet";

    public static final String ADMIN_SERVLET_NAME = "AdeptJ AdminServlet";

    public static final String LOGBACK_STATUS_SERVLET_NAME = "Logback ViewStatusMessagesServlet";

    public static final String KEY_HEALTH_CHECK_HANDLER_PATH = "common.health-check-handler-path";

    public static final String KEY_CONTEXT_PATH = "common.context-path";
}
