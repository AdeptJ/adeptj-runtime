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

import static com.adeptj.modularweb.micro.common.Constants.BUFF_SIZE_1K;
import static com.adeptj.modularweb.micro.common.Constants.BUFF_SIZE_512_BYTES;
import static com.adeptj.modularweb.micro.common.Constants.CMD_LAUNCH_BROWSER;
import static com.adeptj.modularweb.micro.common.Constants.CONTEXT_PATH;
import static com.adeptj.modularweb.micro.common.Constants.DEPLOYMENT_NAME;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_POWERED_BY;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_POWERED_BY_VALUE;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_SERVER;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_SERVER_VALUE;
import static com.adeptj.modularweb.micro.common.Constants.IOTHREAD_MULTIPLIER;
import static com.adeptj.modularweb.micro.common.Constants.KEY_HOST;
import static com.adeptj.modularweb.micro.common.Constants.KEY_HTTP;
import static com.adeptj.modularweb.micro.common.Constants.KEY_PORT;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_BUFFER_SIZE;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_DIRECT_BUFFERS;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_INTERNALS;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_IOTHREADS;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_MAX_MEM_128M;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_MAX_MEM_64M;
import static com.adeptj.modularweb.micro.common.Constants.KEY_UT_WORKER_THREADS;
import static com.adeptj.modularweb.micro.common.Constants.MAX_REQ_WHEN_SHUTDOWN;
import static com.adeptj.modularweb.micro.common.Constants.MIN_PROCESSORS;
import static com.adeptj.modularweb.micro.common.Constants.OSGI_CONSOLE_URL;
import static com.adeptj.modularweb.micro.common.Constants.STARTUP_INFO;
import static com.adeptj.modularweb.micro.common.Constants.SYS_PROP_SERVER_PORT;
import static com.adeptj.modularweb.micro.common.Constants.TRUE;

import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
 * UndertowProvisioner: Provision the UNDERTOW server and OSGi Framework.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class UndertowProvisioner {

	private static final String PROTOCOL_TLS = "TLS";

	private static final Logger LOGGER = LoggerFactory.getLogger(UndertowProvisioner.class);

	public void provision(Map<String, String> arguments) throws Exception {
		Config undertowConf = Configs.INSTANCE.main().getConfig("undertow");
		Config httpConf = undertowConf.getConfig(KEY_HTTP);
		int port = getPort(httpConf);
		LOGGER.info("Starting AdeptJ Modular Web Micro on port: [{}]", port);
		LOGGER.info(CommonUtils.toString(UndertowProvisioner.class.getResourceAsStream(STARTUP_INFO)));
		DeploymentManager manager = Servlets.newContainer().addDeployment(this.constructDeploymentInfo());
		manager.deploy();
		Builder undertowBuilder = this.undertowBuilder(undertowConf).addHttpListener(port,
				httpConf.getString(KEY_HOST));
		this.enableAJP(undertowConf, undertowBuilder);
		this.enableHttp2(undertowConf, undertowBuilder);
		Undertow server = undertowBuilder
				.setHandler(new DelegatingSetHeadersHttpHandler(manager.start(), this.buildHeaders())).build();
		server.start();
		Runtime.getRuntime().addShutdownHook(new UndertowShutdownHook(server, manager));
		if (TRUE.equals(arguments.get(CMD_LAUNCH_BROWSER))) {
			CommonUtils.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL, port)));
		}
	}

	private void enableHttp2(Config undertowConf, Builder undertowBuilder) throws Exception {
		String enableHttp2 = System.getProperty("enable.http2");
		if (TRUE.equalsIgnoreCase(enableHttp2)) {
			Config httpsConf = undertowConf.getConfig("https");
			char[] storePwd = httpsConf.getString("keyStorePwd").toCharArray();
			SSLContext sslContext = this.createSSLContext(this.loadKeyStore(httpsConf.getString("keyStore"), storePwd),
					this.loadKeyStore(httpsConf.getString("trustStore"), storePwd), storePwd);
			int httpsPort = httpsConf.getInt(KEY_PORT);
			undertowBuilder.addHttpsListener(httpsPort, httpsConf.getString(KEY_HOST), sslContext);
			LOGGER.info("HTTP2 enabled on port: [{}]", httpsPort);
		}
	}

	private void enableAJP(Config undertowConf, Builder undertowBuilder) {
		String enableAjp = System.getProperty("enable.ajp");
		if (TRUE.equalsIgnoreCase(enableAjp)) {
			Config ajpConf = undertowConf.getConfig("ajp");
			int ajpPort = ajpConf.getInt(KEY_PORT);
			undertowBuilder.addAjpListener(ajpPort, ajpConf.getString(KEY_HOST));
			LOGGER.info("AJP enabled on port: [{}]", ajpPort);
		}
	}

	private int getPort(Config httpConf) {
		String propertyPort = System.getProperty(SYS_PROP_SERVER_PORT);
		int port;
		if (propertyPort == null || propertyPort.isEmpty()) {
			port = httpConf.getInt(KEY_PORT);
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
		headers.put(HttpString.tryFromString(HEADER_SERVER), HEADER_SERVER_VALUE);
		headers.put(HttpString.tryFromString(HEADER_POWERED_BY), HEADER_POWERED_BY_VALUE);
		return headers;
	}

	private Builder undertowBuilder(Config undertowConf) {
		Builder builder = Undertow.builder();
		int ioThreadsRuntime = Math.max(Runtime.getRuntime().availableProcessors(), MIN_PROCESSORS);
		int workerThreadsRuntime = ioThreadsRuntime * IOTHREAD_MULTIPLIER;
		Config internals = undertowConf.getConfig(KEY_UT_INTERNALS);
		handleIOThreads(builder, ioThreadsRuntime, internals);
		handleWorkerThreads(builder, workerThreadsRuntime, internals);
		handleUndertowBuffer(builder, internals);
		builder.setHandler(gracefulShutdownHandler(null));
		UndertowOptionsBuilder.build(builder, undertowConf);
		return builder;
	}

	private void handleWorkerThreads(Builder builder, int workerThreadsRuntime, Config internals) {
		int workerThreadsConf = internals.getInt(KEY_UT_WORKER_THREADS);
		int workerThreads = workerThreadsRuntime > workerThreadsConf ? workerThreadsRuntime
				: (workerThreadsConf > workerThreadsRuntime ? workerThreadsRuntime : workerThreadsConf);
		builder.setWorkerThreads(workerThreads);
	}

	private void handleIOThreads(Builder builder, int ioThreadsRuntime, Config internals) {
		int ioThreadsConf = internals.getInt(KEY_UT_IOTHREADS);
		int ioThreads = ioThreadsRuntime > ioThreadsConf ? ioThreadsRuntime
				: (ioThreadsConf > ioThreadsRuntime ? ioThreadsRuntime : ioThreadsConf);
		builder.setIoThreads(ioThreads);
	}

	private void handleUndertowBuffer(Builder builder, Config internals) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		// smaller than 64mb of ram we use 512b buffers
		if (maxMemory < internals.getLong(KEY_UT_MAX_MEM_64M)) {
			// use 512b buffers
			builder.setDirectBuffers(false);
			builder.setBufferSize(BUFF_SIZE_512_BYTES);
		} else if (maxMemory < internals.getLong(KEY_UT_MAX_MEM_128M)) {
			// use 1k buffers
			builder.setDirectBuffers(internals.getBoolean(KEY_UT_DIRECT_BUFFERS));
			builder.setBufferSize(BUFF_SIZE_1K);
		} else {
			// use 16k buffers for best performance
			// as 16k is generally the max amount of data that can be sent in a
			// single write() call
			builder.setDirectBuffers(internals.getBoolean(KEY_UT_DIRECT_BUFFERS));
			builder.setBufferSize(internals.getInt(KEY_UT_BUFFER_SIZE));
		}
	}

	private GracefulShutdownHandler gracefulShutdownHandler(PathHandler handler) {
		return new GracefulShutdownHandler(new RequestLimitingHandler(new RequestLimit(MAX_REQ_WHEN_SHUTDOWN),
				new AllowedMethodsHandler(new BlockingHandler(), Verb.GET.toHttpString(), Verb.POST.toHttpString(),
						Verb.PUT.toHttpString(), Verb.DELETE.toHttpString(), Verb.OPTIONS.toHttpString(),
						Verb.PATCH.toHttpString())));
	}

	private DeploymentInfo constructDeploymentInfo() {
		Set<Class<?>> handlesTypes = new HashSet<>();
		handlesTypes.add(FrameworkStartupHandler.class);
		return Servlets.deployment()
				.addServletContainerInitalizer(new ServletContainerInitializerInfo(StartupHandlerInitializer.class,
						new ImmediateInstanceFactory<>(new StartupHandlerInitializer()), handlesTypes))
				.setClassLoader(UndertowProvisioner.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setIgnoreFlush(true).setDeploymentName(DEPLOYMENT_NAME);
	}
	
	private KeyStore loadKeyStore(String name, char[] pwd) throws Exception {
		KeyStore loadedKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
		loadedKeystore.load(UndertowProvisioner.class.getResourceAsStream(name), pwd);
		return loadedKeystore;
	}

	private SSLContext createSSLContext(KeyStore keyStore, KeyStore trustStore, char[] keyStorePwd) throws Exception {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyStorePwd);
		KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		
		SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
		sslContext.init(keyManagers, trustManagers, null);
		return sslContext;
	}
}
