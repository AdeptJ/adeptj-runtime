/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a mapping of {@link ServletConfig} instances.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServletConfigs {

	INSTANCE;

	private Map<String, ServletConfig> configs = new HashMap<>();

	public void add(Class<? extends HttpServlet> klazz, ServletConfig config) {
		this.configs.put(klazz.getName(), config);
	}

	public void remove(Class<? extends HttpServlet> klazz) {
		this.configs.remove(klazz.getName());
	}

	public ServletConfig get(Class<? extends HttpServlet> klazz) {
		return this.configs.get(klazz.getName());
	}
}
