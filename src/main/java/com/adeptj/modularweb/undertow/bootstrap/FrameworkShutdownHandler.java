package com.adeptj.modularweb.undertow.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextListener that handles the OSGi Framework shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkShutdownHandler implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkShutdownHandler.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {
		LOGGER.info("@@@@ FrameworkShutdownHandler.contextInitialized @@@@");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LOGGER.info("Stopping OSGi Framework as ServletContext is being destroyed!!");
		long startTime = System.currentTimeMillis();
		FrameworkBootstrap.INSTANCE.stopFramework();
		ServletContextAware.INSTANCE.setServletContext(null);
		LOGGER.info("OSGi Framework stopped in [{}] ms!!", (System.currentTimeMillis() - startTime));
	}

}
