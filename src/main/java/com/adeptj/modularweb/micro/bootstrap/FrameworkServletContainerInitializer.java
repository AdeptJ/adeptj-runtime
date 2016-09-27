package com.adeptj.modularweb.micro.bootstrap;

import static com.adeptj.modularweb.micro.bootstrap.FrameworkConstants.BUNDLES_ROOT_DIR_KEY;
import static com.adeptj.modularweb.micro.bootstrap.FrameworkConstants.BUNDLES_ROOT_DIR_VALUE;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Initializer that is called by the Container while initialization is in progress.
 * This will further call onStartup method of all of the {@link HandlesTypes} classes registered with this Initializer.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
@HandlesTypes(StartupHandler.class)
public class FrameworkServletContainerInitializer implements ServletContainerInitializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkServletContainerInitializer.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStartup(Set<Class<?>> handlers, ServletContext context) throws ServletException {
		if (handlers == null) {
			// We can't go ahead if FrameworkStartupHandler is not passed by container.
			LOGGER.error("No @HandlesTypes(StartupHandler) on classpath!!");
			throw new IllegalStateException("No @HandlesTypes(StartupHandler) on classpath!!");
		} else {
			ServletContextAware.INSTANCE.setServletContext(context);
			context.setInitParameter(BUNDLES_ROOT_DIR_KEY, BUNDLES_ROOT_DIR_VALUE);
			handlers.forEach(handler -> {
				LOGGER.info("Handling @HandlesTypes: [{}]", handler);
				try {
					if (StartupHandler.class.isAssignableFrom(handler)) {
						StartupHandler.class.cast(handler.newInstance()).onStartup(context);
					} else {
						LOGGER.warn("Unknown @HandlesTypes: [{}]", handler);
					}
				} catch (Exception ex) {
					LOGGER.error("StartupHandler Exception!!", ex);
					throw new RuntimeException("StartupHandler Exception!!", ex);
				}
			});
			// If we are here means startup went well above, register FrameworkShutdownHandler now.
			context.addListener(FrameworkShutdownHandler.class);
		}
	}

}
