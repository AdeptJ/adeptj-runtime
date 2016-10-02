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
package com.adeptj.modularweb.micro.bootstrap.core;

import static com.adeptj.modularweb.micro.bootstrap.common.FrameworkConstants.STARTUP_INFO;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.bootstrap.common.CommonUtils;
import com.adeptj.modularweb.micro.bootstrap.config.Configs;
import com.adeptj.modularweb.micro.bootstrap.undertow.UndertowProvisioner;
import com.typesafe.config.Config;

/**
 * Main class bootstrapping the AdeptJ runtime.
 * 
 * Rakesh.Kumar, AdeptJ
 */
public class Main {
	
	private static final long START_TIME = System.currentTimeMillis();

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			Config rootConf = Configs.INSTANCE.root();
			Config undertowConf = rootConf.getConfig("undertow");
			Config httpConf = undertowConf.getConfig("http");
			int port = getPort(httpConf);
			String startupInfo = CommonUtils.toString(UndertowProvisioner.class.getResourceAsStream(STARTUP_INFO));
			LOGGER.info("Starting AdeptJ Modular Web Micro on port: [{}]", port);
			LOGGER.info(startupInfo);
			UndertowProvisioner provisioner = new UndertowProvisioner();
			provisioner.provision(parseCommands(args), port, undertowConf);
			LOGGER.info("AdeptJ Modular Web Micro Initialized in [{}] ms", (System.currentTimeMillis() - START_TIME));
		} catch (Throwable th) {
			LOGGER.error("Fatal, exiting JVM!!", th);
			System.exit(-1);
		}
	}
	
	private static Map<String, String> parseCommands(String[] args) {
		Map<String, String> arguments = new HashMap<>();
		// Parse the command line.
		return arguments;
	}

	private static int getPort(Config httpConf) {
		String propertyPort = System.getProperty("adeptj.server.port");
		int port;
		if (propertyPort == null || propertyPort.isEmpty()) {
			port = httpConf.getInt("port");
			LOGGER.warn("No port specified, using default port: [{}]", port);
		} else {
			port = Integer.parseInt(propertyPort);
		}
		if (!CommonUtils.isPortAvailable(port)) {
			System.exit(-1);
		}
		return port;
	}

}
