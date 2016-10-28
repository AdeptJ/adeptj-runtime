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
package com.adeptj.modularweb.runtime.undertow;

import java.util.Map;

import org.xnio.Option;

import com.typesafe.config.Config;

import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;

/**
 * UNDERTOW Server Options.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ServerOptions {

	/**
	 * Configures the server options dynamically.
	 * 
	 * @param builder
	 * @param undertowConfig
	 */
	public static void setOptions(Builder builder, Config undertowConfig) {
		Config serverOptions = undertowConfig.getConfig("server-options");
		setStringOptions(builder, serverOptions.getObject("options-type-string").unwrapped());
		setIntOptions(builder, serverOptions.getObject("options-type-integer").unwrapped());
		setLongOptions(builder, serverOptions.getObject("options-type-long").unwrapped());
		setBooleanOptions(builder, serverOptions.getObject("options-type-boolean").unwrapped());
	}
	
	private static void setStringOptions(Builder builder, Map<String, Object> options) {
		options.forEach((key, val) -> {
			builder.setServerOption(Option.simple(UndertowOptions.class, key, String.class), (String) val);
		});
	}
	
	private static void setIntOptions(Builder builder, Map<String, Object> options) {
		options.forEach((key, val) -> {
			builder.setServerOption(Option.simple(UndertowOptions.class, key, Integer.class), Integer.valueOf((String) val));
		});
	}

	private static void setLongOptions(Builder builder, Map<String, Object> options) {
		options.forEach((key, val) -> {
			builder.setServerOption(Option.simple(UndertowOptions.class, key, Long.class), Long.valueOf((String) val));
		});
	}

	private static void setBooleanOptions(Builder builder, Map<String, Object> options) {
		options.forEach((key, val) -> {
			builder.setServerOption(Option.simple(UndertowOptions.class, key, Boolean.class), Boolean.valueOf((Boolean) val));
		});
	}
}
