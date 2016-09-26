package com.adeptj.modularweb.undertow.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StartupHandler is a {@link javax.servlet.annotation.HandlesTypes} that handles the OSGi Framework startup.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@StartupOrder(1)
public class FrameworkStartupHandler implements StartupHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkStartupHandler.class);

    /**
     * This method will be called by the {@link FrameworkServletContainerInitializer} while application startup is in
     * progress.
     *
     * @param context
     * @throws ServletException
     */
    @Override
	public void onStartup(ServletContext context) throws ServletException {
		LOGGER.info("Starting the OSGi Framework!!");
		long startTime = System.currentTimeMillis();
		FrameworkBootstrap.INSTANCE.startFramework();
		LOGGER.info("OSGi Framework started in [{}] ms!!", (System.currentTimeMillis() - startTime));
	}
}
