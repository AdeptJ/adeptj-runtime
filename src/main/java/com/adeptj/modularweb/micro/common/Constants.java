/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
*/
package com.adeptj.modularweb.micro.common;

import java.io.File;

/**
 * Constants.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Constants {

	public static final String BUNDLES_ROOT_DIR_KEY = "BundlesRootDir";

	public static final String BUNDLES_ROOT_DIR_VALUE = File.separator + "bundles" + File.separator;

	public static final String PROVISIONING_FILE = "provisioning";

	public static final String MAIN_CONF_SECTION = "main";

	public static final String CONTEXT_PATH = "/";

	public static final String STARTUP_INFO = "/startup-info.txt";

	public static final String SHUTDOWN_INFO = "/shutdown-info.txt";
	
	public static final String REGEX_EQ = "=";

	public static final String UTF8 = "UTF-8";

	public static final String TRUE = "true";

	public static final String FALSE = "false";

	public static final int MAX_REQ_WHEN_SHUTDOWN = 1000;

	public static final int MIN_PROCESSORS = 2;

	public static final int IOTHREAD_MULTIPLIER = 8;

	public static final int BUFF_SIZE_1K = 1024;

	public static final int BUFF_SIZE_512_BYTES = 512;

	public static final String SYS_PROP_SERVER_PORT = "adeptj.server.port";

	public static final String CMD_LAUNCH_BROWSER = "launchBrowser";

	public static final String DATE_FORMAT_LOGGING = "dd.MM.yyyy HH:mm:ss.SSS ";

	public static final String SHUTDOWN_HOOK = "AdeptJ Modular Web Micro Terminator";

	public static final String DEPLOYMENT_NAME = "AdeptJ Modular Web Micro Deployment";

	public static final String OSGI_CONSOLE_URL = "http://localhost:%s/system/console";

	public static final String WIN_BROWSER_LAUNCH_CMD = "rundll32 url.dll,FileProtocolHandler ";

	public static final String MAC_BROWSER_LAUNCH_CMD = "open ";

	public static final String OS_NAME_PROP = "os.name";

	public static final String OS = System.getProperty(OS_NAME_PROP);

	// Configuration Keys constants start.

	public static final String KEY_BROWSERS = "common.browsers";

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

	public static final String HEADER_POWERED_BY_VALUE = "Undertow";

	public static final String HEADER_SERVER_VALUE = "AdeptJ Modular Web Micro";

	public static final String HEADER_POWERED_BY = "X-Powered-By";

	public static final String HEADER_SERVER = "Server";

	// Configuration Keys constants ends.
}
