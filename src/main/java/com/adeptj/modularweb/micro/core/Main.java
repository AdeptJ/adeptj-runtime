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
package com.adeptj.modularweb.micro.core;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.BundleContextAware;
import com.adeptj.modularweb.micro.common.Constants;
import com.adeptj.modularweb.micro.common.LogbackProvisioner;
import com.adeptj.modularweb.micro.osgi.FrameworkProvisioner;
import com.adeptj.modularweb.micro.undertow.UndertowProvisioner;

/**
 * Entry point for initializing the AdeptJ Runtime.
 * 
 * Rakesh.Kumar, AdeptJ
 */
public final class Main {

	/**
	 * Entry point for initializing the AdeptJ Runtime.
	 * 
	 * It does the following tasks in order.
	 * 
	 * 1. Initializes the LOGBACK logging framework.
	 * 2. Does the deployment to embedded UNDERTOW.
	 * 3. Starts the OSGi Framework.
	 * 4. Starts the UNDERTOW server.
	 * 5. Registers the runtime ShutdownHook. 
	 */
	public static void main(String[] args) {
		long startNanos = System.nanoTime();
		// First of all initialize LOGBACK.
		LogbackProvisioner.start();
		Logger logger = LoggerFactory.getLogger(Main.class);
		try {
			UndertowProvisioner.provision(parseCommands(args));
			logger.info("AdeptJ ModularWeb Micro Initialized in [{}] ms!!", NANOSECONDS.toMillis(System.nanoTime() - startNanos));
		} catch (Throwable th) {
			// Check if OSGi Framework was already started, try to stop the framework gracefully.
			if (BundleContextAware.INSTANCE.getBundleContext() != null) {
				logger.warn("Server startup failed but OSGi Framework was started already, stopping it gracefully!!");
				FrameworkProvisioner.INSTANCE.stopFramework();
			}
			logger.error("Fatal error, shutting down JVM!!", th);
			// Let the LOGBACK cleans up it's state.
			LogbackProvisioner.stop();
			System.exit(-1);
		}
	}

	private static Map<String, String> parseCommands(String[] commands) {
		Map<String, String> arguments = new HashMap<>();
		// Parse the command line.
		for (String cmd : commands) {
			int indexOfEq = cmd.indexOf(Constants.REGEX_EQ);
			arguments.put(cmd.substring(0, indexOfEq), cmd.substring(indexOfEq + 1, cmd.length()));
		}
		return arguments;
	}

}