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
	
	public static final String UTF8 = "UTF-8";
	
	public static final String DATE_FORMAT_LOGGING = "dd.MM.yyyy HH:mm:ss.SSS ";
	
	public static final String SHUTDOWN_HOOK = "AdeptJ Modular Web Micro Terminator"; 
	
	public static final String DEPLOYMENT_NAME = "AdeptJ Modular Web Micro Deployment";
	
	public static final String OSGI_CONSOLE_URL = "http://localhost:%s/system/console";
	
	public static final String WIN_BROWSER_LAUNCH_CMD = "rundll32 url.dll,FileProtocolHandler ";
	
	public static final String MAC_BROWSER_LAUNCH_CMD = "open ";
	
	public static final String OS_NAME_PROP = "os.name";
	
	public static final String OS = System.getProperty(OS_NAME_PROP);
	
	// Configuration Keys constants start.
	
	public static final String CONF_KEY_BROWSERS = "common.browsers";
	
	// Configuration Keys constants ends.
}
