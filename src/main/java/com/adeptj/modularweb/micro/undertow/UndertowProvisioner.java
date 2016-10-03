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

import static com.adeptj.modularweb.micro.common.Constants.CONTEXT_PATH;
import static com.adeptj.modularweb.micro.common.Constants.DEPLOYMENT_NAME;
import static com.adeptj.modularweb.micro.common.Constants.OSGI_CONSOLE_URL;
import static com.adeptj.modularweb.micro.common.Constants.STARTUP_INFO;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.CommonUtils;
import com.adeptj.modularweb.micro.common.Verb;
import com.adeptj.modularweb.micro.config.Configs;
import com.adeptj.modularweb.micro.initializer.StartupHandlerInitializer;
import com.adeptj.modularweb.micro.osgi.FrameworkStartupHandler;
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
 * UndertowProvisioner: Provision the Undertow server and OSGi Framework.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class UndertowProvisioner {

	private static final Logger LOGGER = LoggerFactory.getLogger(UndertowProvisioner.class);

	private Config undertowConf;

	public UndertowProvisioner() {
		this.undertowConf = Configs.INSTANCE.main().getConfig("undertow");
	}

	public void provision(Map<String, String> arguments) {
		try {
			Config httpConf = this.undertowConf.getConfig("http");
			int port = getPort(httpConf);
			LOGGER.info("Starting AdeptJ Modular Web Micro on port: [{}]", port);
			LOGGER.info(CommonUtils.toString(UndertowProvisioner.class.getResourceAsStream(STARTUP_INFO)));
			DeploymentManager manager = Servlets.newContainer().addDeployment(this.constructDeploymentInfo());
			manager.deploy();
			Undertow server = this.undertowBuilder(this.undertowConf).addHttpListener(port, httpConf.getString("host"))
					.setHandler(new DelegatingSetHeadersHttpHandler(manager.start(), this.buildHeaders())).build();
			server.start();
			Runtime.getRuntime().addShutdownHook(new UndertowShutdownHook(server, manager));
            if (arguments.get("launchBrowser") != null) {
                CommonUtils.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL, port)));
            }
		} catch (Throwable ex) {
			LOGGER.error("Problem starting server!!", ex);
			System.exit(-1);
		}
	}

	private int getPort(Config httpConf) {
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

	private Map<HttpString, String> buildHeaders() {
		Map<HttpString, String> headers = new HashMap<>();
		headers.put(HttpString.tryFromString("Server"), "AdeptJ Modular Web Micro");
		headers.put(HttpString.tryFromString("X-Powered-By"), "Undertow");
		return headers;
	}

	private Builder undertowBuilder(Config undertowConf) {
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

	private void handleWorkerThreads(Builder builder, int workerThreadsRuntime, Config internals) {
		int workerThreadsConf = internals.getInt("worker-threads");
		int workerThreads = workerThreadsRuntime > workerThreadsConf ? workerThreadsRuntime
				: (workerThreadsConf > workerThreadsRuntime ? workerThreadsRuntime : workerThreadsConf);
		builder.setWorkerThreads(workerThreads);
	}

	private void handleIOThreads(Builder builder, int ioThreadsRuntime, Config internals) {
		int ioThreadsConf = internals.getInt("io-threads");
		int ioThreads = ioThreadsRuntime > ioThreadsConf ? ioThreadsRuntime
				: (ioThreadsConf > ioThreadsRuntime ? ioThreadsRuntime : ioThreadsConf);
		builder.setIoThreads(ioThreads);
	}

	private void handleUndertowBuffer(Builder builder, Config internals) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		// smaller than 64mb of ram we use 512b buffers
		if (maxMemory < internals.getLong("max-mem-64m")) {
			// use 512b buffers
			builder.setDirectBuffers(false);
			builder.setBufferSize(512);
		} else if (maxMemory < internals.getLong("max-mem-128m")) {
			// use 1k buffers
			builder.setDirectBuffers(internals.getBoolean("direct-buffers"));
			builder.setBufferSize(1024);
		} else {
			// use 16k buffers for best performance
			// as 16k is generally the max amount of data that can be sent in a
			// single write() call
			builder.setDirectBuffers(internals.getBoolean("direct-buffers"));
			builder.setBufferSize(internals.getInt("buffer-size"));
		}
	}

	private GracefulShutdownHandler gracefulShutdownHandler(PathHandler handler) {
		return new GracefulShutdownHandler(new RequestLimitingHandler(new RequestLimit(1000),
				new AllowedMethodsHandler(new BlockingHandler(), Verb.GET.toHttpString(), Verb.POST.toHttpString(),
						Verb.PUT.toHttpString(), Verb.DELETE.toHttpString(), Verb.OPTIONS.toHttpString(),
						Verb.PATCH.toHttpString())));
	}

	private DeploymentInfo constructDeploymentInfo() {
		Set<Class<?>> handlesTypes = new HashSet<>();
		handlesTypes.add(FrameworkStartupHandler.class);
		return Servlets.deployment()
				.addServletContainerInitalizer(new ServletContainerInitializerInfo(
						StartupHandlerInitializer.class,
						new ImmediateInstanceFactory<>(new StartupHandlerInitializer()), handlesTypes))
				.setClassLoader(UndertowProvisioner.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setIgnoreFlush(true).setDeploymentName(DEPLOYMENT_NAME);
	}
}
