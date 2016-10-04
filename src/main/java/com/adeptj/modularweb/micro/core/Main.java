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
package com.adeptj.modularweb.micro.core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.LogbackInitializer;
//import com.adeptj.modularweb.micro.common.LogbackInitializer;
import com.adeptj.modularweb.micro.undertow.UndertowProvisioner;

/**
 * Main class bootstrapping the AdeptJ runtime.
 * 
 * Rakesh.Kumar, AdeptJ
 */
public class Main {

	public static final String REGEX_EQ = "=";

	private static final long START_TIME = System.currentTimeMillis();

	private static final Logger LOGGER;

	static {
		LogbackInitializer.init();
		LOGGER = LoggerFactory.getLogger(Main.class);
	}

	public static void main(String[] args) {
		try {
			UndertowProvisioner provisioner = new UndertowProvisioner();
			provisioner.provision(parseCommands(args));
			LOGGER.info("AdeptJ Modular Web Micro Initialized in [{}] ms", (System.currentTimeMillis() - START_TIME));
		} catch (Throwable th) {
			LOGGER.error("Fatal, exiting JVM!!", th);
			System.exit(-1);
		}
	}

	private static Map<String, String> parseCommands(String[] commands) {
		Map<String, String> arguments = new HashMap<>();
		// Parse the command line.
		for (String cmd : commands) {
			int indexOf = cmd.indexOf(REGEX_EQ);
			arguments.put(cmd.substring(0, indexOf), cmd.substring(indexOf + 1, cmd.length()));
		}
		return arguments;
	}

}
