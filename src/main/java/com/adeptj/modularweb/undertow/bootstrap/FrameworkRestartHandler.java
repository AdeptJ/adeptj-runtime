/**
 * 
 */
package com.adeptj.modularweb.undertow.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi FrameworkListener.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkRestartHandler implements FrameworkListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkRestartHandler.class);

	private FrameworkServlet frameworkServlet;

	public FrameworkRestartHandler(FrameworkServlet frameworkServlet) {
		this.frameworkServlet = frameworkServlet;
	}

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		int type = event.getType();
		switch (type) {
		case FrameworkEvent.STARTED:
			LOGGER.info("System Bundle Started!!");
			// Add the new BundleContext as a ServletContext attribute replacing the stale BundleContext.
			ServletContext servletContext = ServletContextAware.INSTANCE.getServletContext();
            servletContext.removeAttribute(BundleContext.class.getName());
			BundleContext bundleContext = event.getBundle().getBundleContext();
			servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
			FrameworkBootstrap.INSTANCE.setSystemBundleContext(bundleContext);
			try {
				this.frameworkServlet.disposeTracker();
				this.frameworkServlet.init();
			} catch (ServletException ex) {
				LOGGER.error("ServletException!!", ex);
			}
			break;
		case FrameworkEvent.STOPPED_UPDATE:
			LOGGER.info("Disposing DispatcherServletTracker!!");
			this.frameworkServlet.disposeTracker();
			break;
		default:
			// log it and ignore.
			LOGGER.warn("Ignoring the FrameworkEvent: [{}]", type);
			break;
		}
	}

}
