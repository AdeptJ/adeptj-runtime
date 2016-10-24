/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, Rakesh Kumar, AdeptJ (http://adeptj.com)                 #
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
package com.adeptj.modularweb.micro.config;

import static com.adeptj.modularweb.micro.common.Constants.COMMON_CONF_SECTION;
import static com.adeptj.modularweb.micro.common.Constants.FELIX_CONF_SECTION;
import static com.adeptj.modularweb.micro.common.Constants.MAIN_CONF_SECTION;
import static com.adeptj.modularweb.micro.common.Constants.PROVISIONING_FILE;
import static com.adeptj.modularweb.micro.common.Constants.UNDERTOW_CONF_SECTION;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Initializes the application configurations.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Configs {

	INSTANCE;

	private final Config main;

	Configs() {
		this.main = ConfigFactory.load(PROVISIONING_FILE).getConfig(MAIN_CONF_SECTION);
	}
	
	public Config undertow() {
		return this.main.getConfig(UNDERTOW_CONF_SECTION);
	}
	
	public Config felix() {
		return this.main.getConfig(FELIX_CONF_SECTION);
	}
	
	public Config common() {
		return this.main.getConfig(COMMON_CONF_SECTION);
	}
}
