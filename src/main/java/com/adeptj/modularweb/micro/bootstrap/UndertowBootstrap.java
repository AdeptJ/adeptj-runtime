package com.adeptj.modularweb.micro.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.Undertow;
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

	private static int port = 8080;

	private static Undertow server;

	private static DeploymentManager manager;

	private static final Logger LOGGER = LoggerFactory.getLogger(UndertowBootstrap.class);

	public static void main(String[] args) throws ServletException {
		LOGGER.info("@@@@@@ Bootstraping AdeptJ Modular Web Undertow @@@@@@");
		Set<Class<?>> handlesTypes = new HashSet<>();
		handlesTypes.add(FrameworkStartupHandler.class);
		DeploymentInfo deploymentInfo = constructDeploymentInfo(
				new ServletContainerInitializerInfo(FrameworkServletContainerInitializer.class,
						new ImmediateInstanceFactory<>(new FrameworkServletContainerInitializer()), handlesTypes));
		manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
		manager.deploy();
		server = Undertow.builder().addHttpListener(port, "localhost").setHandler(manager.start()).build();
		server.start();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		LOGGER.info("@@@@@@ AdeptJ Modular Web Undertow Initialized!! @@@@@@");
	}

	private static class ShutdownHook extends Thread {
		@Override
		public void run() {
			LOGGER.info("@@@@ Stopping Undertow!! @@@@");
			manager.undeploy();
			server.stop();
		}

	}

	private static DeploymentInfo constructDeploymentInfo(ServletContainerInitializerInfo sciInfo) {
		return Servlets.deployment().addServletContainerInitalizer(sciInfo)
				.setClassLoader(UndertowBootstrap.class.getClassLoader()).setContextPath("/")
				.setDeploymentName("AdeptJ Modular Web Undertow");
	}
}
