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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.RequestLimit;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.util.HttpString;

/**
 * UndertowBootstrap: Bootstrap the Undertow server and OSGi Framework.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class UndertowBootstrap {

	private static final long START_TIME = System.currentTimeMillis();
	
	private static final String STARTUP_INFO = "/startup-info.txt";

	private static final String SHUTDOWN_INFO = "/shutdown-info.txt";

	private static final Logger LOGGER = LoggerFactory.getLogger(UndertowBootstrap.class);

	private static final String CONTEXT_PATH = "/";

	private static final String UTF8 = StandardCharsets.UTF_8.name();

	private static Undertow server;

	private static DeploymentManager manager;
	
	public static void main(String[] args) {
		try {
			Config rootConf = Configs.INSTANCE.root();
			Config undertowConf = rootConf.getConfig("undertow");
			Config httpConf = undertowConf.getConfig("http");
			//String startupLogFile = rootConf.getConfig("common").getString("startup-log-file");
			//Files.exists(Paths.get(startupLogFile));
			String propertyPort = System.getProperty("adeptj.server.port");
			int port;
			if (propertyPort == null || propertyPort.isEmpty()) {
				port = httpConf.getInt("port");
				LOGGER.warn("No port specified, using default port: [{}]", port);
			} else {
				port = Integer.parseInt(propertyPort);
			}
			if (!isPortAvailable(port)) {
				System.exit(-1);
			}
			String startupInfo = stringify(UndertowBootstrap.class.getResourceAsStream(STARTUP_INFO));
			LOGGER.info("Starting AdeptJ Modular Web Micro on port: [{}]", port);
			LOGGER.info(startupInfo);
			// Order of StartupHandler matters.
			Set<Class<?>> handlesTypes = new LinkedHashSet<>();
			handlesTypes.add(FrameworkStartupHandler.class);
			DeploymentInfo deploymentInfo = constructDeploymentInfo(
					new ServletContainerInitializerInfo(FrameworkServletContainerInitializer.class,
							new ImmediateInstanceFactory<>(new FrameworkServletContainerInitializer()), handlesTypes));
			deploymentInfo.setIgnoreFlush(true);
			manager = Servlets.newContainer().addDeployment(deploymentInfo);
			manager.deploy();
			Map<HttpString, String> headers = new HashMap<>();
			headers.put(HttpString.tryFromString("Server"), "AdeptJ Modular Web Micro");
			headers.put(HttpString.tryFromString("X-Powered-By"), "Undertow");
			server = undertowBuilder(undertowConf).addHttpListener(port, httpConf.getString("host"))
					.setHandler(new FrameworkHttpHandler(manager.start(), headers)).build();
			server.start();
			Runtime.getRuntime().addShutdownHook(new ShutdownHook("AdeptJ Modular Web Micro Terminator"));
			if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
				Runtime.getRuntime().exec("open " + "http://" + "localhost" + ":" + port + "/system/console");
			}
			LOGGER.info("AdeptJ Modular Web Micro Initialized in [{}] ms", (System.currentTimeMillis() - START_TIME));
		} catch (Throwable ex) {
			LOGGER.error("Unexpected!!", ex);
			System.exit(-1);
		}
	}

	/**
	 * ShutdownHook for handling CTRL+C, this first un-deploy the app and then
	 * stops Undertow server.
	 * 
	 * Rakesh.Kumar, AdeptJ
	 */
	private static class ShutdownHook extends Thread {

		public ShutdownHook(String name) {
			super(name);
		}

		/**
		 * Handles Graceful server shutdown and resource cleanup.
		 */
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			LOGGER.info("Stopping AdeptJ Modular Web Micro!!");
			try {
				manager.stop();
				manager.undeploy();
			} catch (ServletException ex) {
				LOGGER.error("Exception while stopping DeploymentManager!!", ex);
			}
			try {
				server.stop();
			} catch (Throwable th) {
				LOGGER.error("Fatal error while stopping AdeptJ Modular Web Micro!!", th);
				System.exit(-1);
			}
			try {
				LOGGER.info(stringify(UndertowBootstrap.class.getResourceAsStream(SHUTDOWN_INFO)));
			} catch (Exception ex) {
				LOGGER.info("AdeptJ Modular Web Micro stopped in [{}] ms", (System.currentTimeMillis() - startTime));
				System.exit(-1);
			}
			LOGGER.info("AdeptJ Modular Web Micro stopped in [{}] ms", (System.currentTimeMillis() - startTime));
		}
	}
	
	public static boolean isPortAvailable(int port) {
		boolean isAvailable = false;
		try (ServerSocketChannel channel = ServerSocketChannel.open()) {
			channel.socket().setReuseAddress(true);
			channel.socket().bind(new InetSocketAddress(port));
			isAvailable = true;
		} catch (IOException ex) {
			LOGGER.error("Exception while aquiring port: [{}], cause:", port, ex);
			isAvailable = !(ex instanceof BindException);
		}
		return isAvailable;
	}

	private static String stringify(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((length = inputStream.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		return out.toString(UTF8);
	}

	/**
	 * Build the Undertow internals.
	 */
	private static Builder undertowBuilder(Config undertowConf) {
		Builder builder = Undertow.builder();
		int ioThreadsRuntime = Math.max(Runtime.getRuntime().availableProcessors(), 2);
        int workerThreadsRuntime = ioThreadsRuntime * 8;
		Config internals = undertowConf.getConfig("internals");
		handleIOThreads(builder, ioThreadsRuntime, internals);
		handleWorkerThreads(builder, workerThreadsRuntime, internals);
		handleUndertowBuffer(builder, internals);
		builder.setHandler(gracefulShutdownHandler(null));
		UndertowOptionsBuilder.build(builder, undertowConf);
		return builder;
	}

	private static void handleWorkerThreads(Builder builder, int workerThreadsRuntime, Config internals) {
		int workerThreadsConf = internals.getInt("worker-threads");
		int workerThreads = workerThreadsRuntime > workerThreadsConf ? workerThreadsRuntime
				: (workerThreadsConf > workerThreadsRuntime ? workerThreadsRuntime : workerThreadsConf);
		builder.setWorkerThreads(workerThreads);
	}

	private static void handleIOThreads(Builder builder, int ioThreadsRuntime, Config internals) {
		int ioThreadsConf = internals.getInt("io-threads");
		int ioThreads = ioThreadsRuntime > ioThreadsConf ? ioThreadsRuntime
				: (ioThreadsConf > ioThreadsRuntime ? ioThreadsRuntime : ioThreadsConf);
		builder.setIoThreads(ioThreads);
	}
	
	private static void handleUndertowBuffer(Builder builder, Config internals) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		//smaller than 64mb of ram we use 512b buffers
        if (maxMemory < internals.getLong("max-mem-64m")) {
            //use 512b buffers
        	builder.setDirectBuffers(false);
        	builder.setBufferSize(512);
        } else if (maxMemory < internals.getLong("max-mem-128m")) {
            //use 1k buffers
            builder.setDirectBuffers(internals.getBoolean("direct-buffers"));
        	builder.setBufferSize(1024);
        } else {
            //use 16k buffers for best performance
            //as 16k is generally the max amount of data that can be sent in a single write() call
            builder.setDirectBuffers(internals.getBoolean("direct-buffers"));
        	builder.setBufferSize(internals.getInt("buffer-size"));
        }
	}

	/**
	 * gracefulShutdownHandler
	 *
	 * @param paths
	 * @return GracefulShutdownHandler
	 */
	private static GracefulShutdownHandler gracefulShutdownHandler(PathHandler paths) {
		return new GracefulShutdownHandler(new RequestLimitingHandler(new RequestLimit(1000),
				new AllowedMethodsHandler(new BlockingHandler(), HttpString.tryFromString("GET"),
						HttpString.tryFromString("POST"), HttpString.tryFromString("PUT"),
						HttpString.tryFromString("DELETE"), HttpString.tryFromString("PATCH"),
						HttpString.tryFromString("OPTIONS"))));
	}

	private static DeploymentInfo constructDeploymentInfo(ServletContainerInitializerInfo sciInfo) {
		return Servlets.deployment().addServletContainerInitalizer(sciInfo)
				.setClassLoader(UndertowBootstrap.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setDeploymentName("AdeptJ Modular Web Micro");
	}
}
