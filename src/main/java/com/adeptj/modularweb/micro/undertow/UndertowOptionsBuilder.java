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
package com.adeptj.modularweb.micro.undertow;

import java.util.HashSet;
import java.util.Set;

import org.xnio.Option;

import com.typesafe.config.Config;

import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;

/**
 * Undertow Server Options.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class UndertowOptionsBuilder {

	private static final Set<Option<Long>> OPTIONS_LONG;

	private static final Set<Option<Integer>> OPTIONS_INT;

	private static final Set<Option<Boolean>> OPTIONS_BOOL;

	static {
		OPTIONS_LONG = new HashSet<>();
		OPTIONS_LONG.add(UndertowOptions.MAX_ENTITY_SIZE);
		OPTIONS_LONG.add(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE);

		OPTIONS_INT = new HashSet<>();
		OPTIONS_INT.add(UndertowOptions.MAX_HEADER_SIZE);

		OPTIONS_BOOL = new HashSet<>();
		OPTIONS_BOOL.add(UndertowOptions.ALWAYS_SET_KEEP_ALIVE);
		OPTIONS_BOOL.add(UndertowOptions.ALWAYS_SET_DATE);
		OPTIONS_BOOL.add(UndertowOptions.ENABLE_HTTP2);
	}

	public static void build(Builder builder, Config undertowConf) {
		Config connConfig = undertowConf.getConfig("connection-options");
		OPTIONS_LONG.forEach(option -> builder.setServerOption(option, connConfig.getLong(option.getName())));
		OPTIONS_INT.forEach(option -> builder.setServerOption(option, connConfig.getInt(option.getName())));
		OPTIONS_BOOL.forEach(option -> builder.setServerOption(option, connConfig.getBoolean(option.getName())));
	}
}
