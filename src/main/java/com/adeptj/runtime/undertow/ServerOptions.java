/** 
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
package com.adeptj.runtime.undertow;

import com.adeptj.runtime.common.TimeUnits;
import com.typesafe.config.Config;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Option;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * UNDERTOW Server Options.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ServerOptions {

	/**
	 * Configures the server options dynamically.
	 * 
	 * @param builder Undertow.Builder
	 * @param undertowConfig Undertow Typesafe Config
	 */
	public static void build(Builder builder, Config undertowConfig) {
		long startTime = System.nanoTime();
		Logger logger = LoggerFactory.getLogger(ServerOptions.class);
		Config serverOptionsCfg = undertowConfig.getConfig("server-options");
		stringOptions(builder, serverOptionsCfg.getObject("options-type-string").unwrapped(), logger);
		integerOptions(builder, serverOptionsCfg.getObject("options-type-integer").unwrapped(), logger);
		longOptions(builder, serverOptionsCfg.getObject("options-type-long").unwrapped(), logger);
		booleanOptions(builder, serverOptionsCfg.getObject("options-type-boolean").unwrapped(), logger);
		logger.info("ServerOptions populated in [{}] ms!!", TimeUnits.nanosToMillis(startTime));
	}
	
	private static void buildServerOptions(Builder builder, Map<String, ?> options, Logger logger) {
		options.forEach((optKey, optVal) -> builder.setServerOption(toOption(optKey, logger), optVal));
	}

	private static void stringOptions(Builder builder, Map<String, ?> options, Logger logger) {
		buildServerOptions(builder, options, logger);
	}

	private static void integerOptions(Builder builder, Map<String, ?> options, Logger logger) {
		buildServerOptions(builder, options, logger);
	}

	private static void booleanOptions(Builder builder, Map<String, ?> options, Logger logger) {
		buildServerOptions(builder, options, logger);
	}

	private static void longOptions(Builder builder, Map<String, ?> options, Logger logger) {
		options.forEach((optKey, optVal) -> builder.setServerOption(toOption(optKey, logger), Long.valueOf((String) optVal)));
	}

	@SuppressWarnings("unchecked")
	private static <T> Option<T> toOption(String name, Logger logger) {
		Option<T> option = null;
		try {
			Field field = UndertowOptions.class.getField(name);
			if (field == null || !field.getName().equals(name)) {
				logger.warn("No such field: [{}] in class: [{}]", name, UndertowOptions.class.getName());
			} else {
				option = (Option<T>) field.get(null);
			}
		} catch (Exception ex) {
			logger.error("Exception!!", ex);
		}
		return option;
	}
}
