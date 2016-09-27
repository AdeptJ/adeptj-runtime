package com.adeptj.modularweb.micro.bootstrap;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

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
		Builder builder = Undertow.builder();
		builder.setBufferSize(config.bufferSize());
		builder.setIoThreads(config.ioThreads());
		builder.setWorkerThreads(config.workerThreads());
		builder.setDirectBuffers(true);
		server = builder.addHttpListener(config.httpPort(), config.httpHost()).setHandler(manager.start()).build();
		server.start();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		LOGGER.info("@@@@@@ AdeptJ Modular Web Micro Initialized!! @@@@@@");
	}

	/**
	 * ShutdownHook for handling CTRL+C, this first un-deploy the app and then stops Undertow server.
	 * 
	 * Rakesh.Kumar, AdeptJ
	 */
	private static class ShutdownHook extends Thread {
		@Override
		public void run() {
			LOGGER.info("@@@@ Stopping AdeptJ Modular Web Micro!! @@@@");
			try {
				manager.stop();
				manager.undeploy();
			} catch (ServletException ex) {
				LOGGER.info("Exception while stopping DeploymentManager!!", ex);
			}
			server.stop();
		}

	}

	private static DeploymentInfo constructDeploymentInfo(ServletContainerInitializerInfo sciInfo) {
		return Servlets.deployment().addServletContainerInitalizer(sciInfo)
				.setClassLoader(UndertowBootstrap.class.getClassLoader()).setContextPath(CONTEXT_PATH)
				.setDeploymentName("AdeptJ Modular Web Micro");
	}
}
