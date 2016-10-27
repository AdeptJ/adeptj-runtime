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
package com.adeptj.modularweb.micro.undertow;

import static com.adeptj.modularweb.micro.common.Constants.CMD_LAUNCH_BROWSER;
import static com.adeptj.modularweb.micro.common.Constants.CONTEXT_PATH;
import static com.adeptj.modularweb.micro.common.Constants.DEPLOYMENT_NAME;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_POWERED_BY;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_POWERED_BY_VALUE;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_SERVER;
import static com.adeptj.modularweb.micro.common.Constants.HEADER_SERVER_VALUE;
import static com.adeptj.modularweb.micro.common.Constants.KEY_HOST;
import static com.adeptj.modularweb.micro.common.Constants.KEY_HTTP;
import static com.adeptj.modularweb.micro.common.Constants.KEY_PORT;
import static com.adeptj.modularweb.micro.common.Constants.MAX_REQ_WHEN_SHUTDOWN;
import static com.adeptj.modularweb.micro.common.Constants.OSGI_CONSOLE_URL;
import static com.adeptj.modularweb.micro.common.Constants.STARTUP_INFO;
import static com.adeptj.modularweb.micro.common.Constants.SYS_PROP_SERVER_PORT;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;

import com.adeptj.modularweb.micro.common.CommonUtils;
import com.adeptj.modularweb.micro.common.ServerMode;
import com.adeptj.modularweb.micro.common.Verb;
import com.adeptj.modularweb.micro.config.Configs;
import com.adeptj.modularweb.micro.initializer.StartupHandlerInitializer;
import com.adeptj.modularweb.micro.logging.LogbackProvisioner;
import com.adeptj.modularweb.micro.osgi.FrameworkStartupHandler;
import com.typesafe.config.Config;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
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
public final class UndertowProvisioner {
	
	// No instantiation.
	private UndertowProvisioner() {}

	private static final String PROTOCOL_TLS = "TLS";

	public static void provision(Map<String, String> arguments) throws Exception {
		Config undertowConf = Configs.INSTANCE.undertow();
		Config httpConf = undertowConf.getConfig(KEY_HTTP);
		Logger logger = LoggerFactory.getLogger(UndertowProvisioner.class);
		int port = getPort(httpConf, logger);
		logger.info("Starting AdeptJ ModularWeb Micro on port: [{}]", port);
		logger.info(CommonUtils.toString(UndertowProvisioner.class.getResourceAsStream(STARTUP_INFO)));
		Builder undertowBuilder = Undertow.builder();
		handleProdMode(undertowBuilder, undertowConf, logger);
		undertowBuilder.addHttpListener(port, httpConf.getString(KEY_HOST));
		UndertowOptionsBuilder.build(undertowBuilder, undertowConf);
		enableAJP(undertowConf, undertowBuilder, logger);
		enableHttp2(undertowConf, undertowBuilder, logger);
		DeploymentManager manager = Servlets.newContainer().addDeployment(constructDeploymentInfo());
		manager.deploy();
		DelegatingSetHeadersHttpHandler rootHandler = new DelegatingSetHeadersHttpHandler(manager.start(), buildHeaders());
		Undertow server = undertowBuilder.setHandler(gracefulShutdownHandler(rootHandler)).build();
		server.start();
		Runtime.getRuntime().addShutdownHook(new UndertowShutdownHook(server, manager));
		if (Boolean.parseBoolean(arguments.get(CMD_LAUNCH_BROWSER))) {
			CommonUtils.launchBrowser(new URL(String.format(OSGI_CONSOLE_URL, port)));
		}
	}
	
	private static void handleProdMode(Builder undertowBuilder, Config undertowConf, Logger logger) {
		if (ServerMode.PROD.toString().equalsIgnoreCase(System.getProperty("adeptj.server.mode"))) {
			logger.info("Provisioning AdeptJ ModularWeb for [PROD] mode.");
			Config workerOptions = undertowConf.getConfig("workerOptions");
			// defaults to 64
			int coreTaskThreadsConfig = workerOptions.getInt("worker-task-core-threads");
			// defaults to double of [worker-task-core-threads] i.e 128
			int maxTaskThreadsConfig = workerOptions.getInt("worker-task-max-threads");
			int sysTaskThreads = Runtime.getRuntime().availableProcessors() * 8;
			if (sysTaskThreads > coreTaskThreadsConfig) {
				undertowBuilder.setWorkerOption(Options.WORKER_TASK_CORE_THREADS, sysTaskThreads);
			} else {
				undertowBuilder.setWorkerOption(Options.WORKER_TASK_CORE_THREADS, coreTaskThreadsConfig);
			}
			int calcMaxTaskThreadsConfig = sysTaskThreads * 2;
			if (calcMaxTaskThreadsConfig > maxTaskThreadsConfig) {
				undertowBuilder.setWorkerOption(Options.WORKER_TASK_MAX_THREADS, calcMaxTaskThreadsConfig);
			} else {
				undertowBuilder.setWorkerOption(Options.WORKER_TASK_MAX_THREADS, maxTaskThreadsConfig);
			}
		} else {
			logger.info("Provisioning AdeptJ ModularWeb for [DEV] mode.");
		}
	}

	private static void enableHttp2(Config undertowConf, Builder undertowBuilder, Logger logger) throws Exception {
		if (Boolean.getBoolean("enable.http2")) {
			Config httpsConf = undertowConf.getConfig("https");
			char[] keyStorePwd = httpsConf.getString("keyStorePwd").toCharArray();
			char[] keyPwd = httpsConf.getString("keyPwd").toCharArray();
			int httpsPort = httpsConf.getInt(KEY_PORT);
			undertowBuilder.addHttpsListener(httpsPort, httpsConf.getString(KEY_HOST),
					sslContext(keyStore(httpsConf.getString("keyStore"), keyStorePwd), keyPwd));
			logger.info("HTTP2 enabled on port: [{}]", httpsPort);
		}
	}

	private static void enableAJP(Config undertowConf, Builder undertowBuilder, Logger logger) {
		if (Boolean.getBoolean("enable.ajp")) {
			Config ajpConf = undertowConf.getConfig("ajp");
			int ajpPort = ajpConf.getInt(KEY_PORT);
			undertowBuilder.addAjpListener(ajpPort, ajpConf.getString(KEY_HOST));
			logger.info("AJP enabled on port: [{}]", ajpPort);
		}
	}

	private static int getPort(Config httpConf, Logger logger) {
		String propertyPort = System.getProperty(SYS_PROP_SERVER_PORT);
		int port;
		if (propertyPort == null || propertyPort.isEmpty()) {
			port = httpConf.getInt(KEY_PORT);
			logger.warn("No port specified via system property: [{}], using default port: [{}]", SYS_PROP_SERVER_PORT, port);
		} else {
			port = Integer.parseInt(propertyPort);
		}
		if (!isPortAvailable(port, logger)) {
			// Let the LOGBACK cleans up it's state.
			logger.error("JVM shutting down!!");
			LogbackProvisioner.stop();
			System.exit(-1);
		}
		return port;
	}
	
	private static boolean isPortAvailable(int port, Logger logger) {
		boolean isPortAvailable = false;
		try (ServerSocketChannel channel = ServerSocketChannel.open()) {
			channel.socket().setReuseAddress(true);
			channel.socket().bind(new InetSocketAddress(port));
			isPortAvailable = true;
		} catch (BindException ex) {
			logger.error("BindException while aquiring port: [{}], cause:", port, ex);
			isPortAvailable = false;
		} catch (IOException ex) {
			logger.error("IOException while aquiring port: [{}], cause:", port, ex);
			isPortAvailable = false;
		}
		return isPortAvailable;
	}

	private static Map<HttpString, String> buildHeaders() {
		Map<HttpString, String> headers = new HashMap<>();
		headers.put(HttpString.tryFromString(HEADER_SERVER), HEADER_SERVER_VALUE);
		headers.put(HttpString.tryFromString(HEADER_POWERED_BY), HEADER_POWERED_BY_VALUE);
		return headers;
	}

	private static GracefulShutdownHandler gracefulShutdownHandler(HttpHandler rootHandler) {
		return Handlers.gracefulShutdown(new RequestLimitingHandler(new RequestLimit(MAX_REQ_WHEN_SHUTDOWN),
				new AllowedMethodsHandler(rootHandler, Verb.allowedMethods())));
	}

	private static DeploymentInfo constructDeploymentInfo() {
		Set<Class<?>> handlesTypes = new HashSet<>();
		handlesTypes.add(FrameworkStartupHandler.class);
		return Servlets.deployment()
				.addServletContainerInitalizer(new ServletContainerInitializerInfo(StartupHandlerInitializer.class,
						new ImmediateInstanceFactory<>(new StartupHandlerInitializer()), handlesTypes))
				.setClassLoader(UndertowProvisioner.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setIgnoreFlush(true).setDeploymentName(DEPLOYMENT_NAME);
	}

	private static KeyStore keyStore(String keyStoreName, char[] keyStorePwd) throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(UndertowProvisioner.class.getResourceAsStream(keyStoreName), keyStorePwd);
		return keyStore;
	}

	private static SSLContext sslContext(KeyStore keyStore, char[] keyPwd) throws Exception {
		SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TLS);
		sslContext.init(keyMgrs(keyStore, keyPwd), null, null);
		return sslContext;
	}

	private static KeyManager[] keyMgrs(KeyStore keyStore, char[] keyPwd) throws Exception {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyPwd);
		return kmf.getKeyManagers();
	}
}
