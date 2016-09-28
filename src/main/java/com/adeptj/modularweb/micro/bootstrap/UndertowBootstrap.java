package com.adeptj.modularweb.micro.bootstrap;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final String CONTEXT_PATH = "/";

	private static Undertow server;

	private static DeploymentManager manager;
	private static final Logger LOGGER = LoggerFactory.getLogger(UndertowBootstrap.class);

	public static void main(String[] args) throws ServletException {
		LOGGER.info("@@@@@@ Bootstraping AdeptJ Modular Web Micro @@@@@@");
		try {
			long startTime = System.currentTimeMillis();
			// Order of StartupHandler matters.
			Set<Class<?>> handlesTypes = new LinkedHashSet<>();
			handlesTypes.add(FrameworkStartupHandler.class);
			DeploymentInfo deploymentInfo = constructDeploymentInfo(
					new ServletContainerInitializerInfo(FrameworkServletContainerInitializer.class,
							new ImmediateInstanceFactory<>(new FrameworkServletContainerInitializer()), handlesTypes));
			deploymentInfo.setIgnoreFlush(true);
			manager = Servlets.newContainer().addDeployment(deploymentInfo);
			manager.deploy();
			Configs config = Configs.INSTANCE;
			server = undertowBuilder(config).addHttpListener(config.httpPort(), config.httpHost())
					.setHandler(manager.start()).build();
			server.start();
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
			long endTime = System.currentTimeMillis() - startTime;
			LOGGER.info("@@@@@@ AdeptJ Modular Web Micro Initialized in [{}] ms @@@@@@", endTime);
		} catch (Throwable ex) {
			LOGGER.error("Unexpected!!", ex);
		}
	}

	/**
	 * ShutdownHook for handling CTRL+C, this first un-deploy the app and then stops Undertow server.
	 * 
	 * Rakesh.Kumar, AdeptJ
	 */
	private static class ShutdownHook extends Thread {
		
		/**
		 * Handles CTRL+C
		 */
		@Override
		public void run() {
			LOGGER.info("@@@@ Stopping AdeptJ Modular Web Micro!! @@@@");
			try {
				manager.stop();
				manager.undeploy();
			} catch (ServletException ex) {
				LOGGER.error("Exception while stopping DeploymentManager!!", ex);
			}
			server.stop();
		}

	}
	
	/**
	 *  Build the Undertow internals.
	 */
	private static Builder undertowBuilder(Configs config) {
		Builder builder = Undertow.builder();
		builder.setBufferSize(config.bufferSize());
		builder.setIoThreads(config.ioThreads());
		builder.setWorkerThreads(config.workerThreads());
		builder.setDirectBuffers(true);
		builder.setHandler(gracefulShutdownHandler(null));
		return builder;
	}
	
	/**
	 * gracefulShutdownHandler
	 *
	 * @param paths
	 * @return GracefulShutdownHandler
	 */
	private static GracefulShutdownHandler gracefulShutdownHandler(PathHandler paths) {
		return new GracefulShutdownHandler(new RequestLimitingHandler(new RequestLimit(1000),
				new AllowedMethodsHandler(new BlockingHandler(),
						HttpString.tryFromString("GET"), HttpString.tryFromString("POST"),
						HttpString.tryFromString("PUT"), HttpString.tryFromString("DELETE"),
						HttpString.tryFromString("PATCH"), HttpString.tryFromString("OPTIONS"))));
	}

	private static DeploymentInfo constructDeploymentInfo(ServletContainerInitializerInfo sciInfo) {
		return Servlets.deployment().addServletContainerInitalizer(sciInfo)
				.setClassLoader(UndertowBootstrap.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setDeploymentName("AdeptJ Modular Web Micro");
	}
}
