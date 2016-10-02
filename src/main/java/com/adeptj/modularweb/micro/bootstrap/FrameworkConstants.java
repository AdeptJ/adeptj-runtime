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
package com.adeptj.modularweb.micro.bootstrap;

import java.io.File;

/**
 * FrameworkConstants.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public final class FrameworkConstants {

	public static final String BUNDLES_ROOT_DIR_KEY = "BundlesRootDir";

	public static final String BUNDLES_ROOT_DIR_VALUE = File.separator + "bundles" + File.separator;
	
	public static final String CONF_FILE = "bootstrap";
	
	public static final String ROOT_CONF = "root";
	
	public static final String STARTUP_INFO = "/startup-info.txt";
	
	public static final String SHUTDOWN_INFO = "/shutdown-info.txt";
	
	public static final String UTF8 = "UTF-8";
}
