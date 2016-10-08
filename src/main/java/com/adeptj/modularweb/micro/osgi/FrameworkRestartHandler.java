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
package com.adeptj.modularweb.micro.osgi;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;
import com.adeptj.modularweb.micro.servlet.ProxyDispatcherServlet;

/**
 * OSGi FrameworkListener.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkRestartHandler implements FrameworkListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkRestartHandler.class);

	private ProxyDispatcherServlet proxyDispatcherServlet;

	public FrameworkRestartHandler(ProxyDispatcherServlet proxyDispatcherServlet) {
		this.proxyDispatcherServlet = proxyDispatcherServlet;
	}

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		int type = event.getType();
		switch (type) {
		case FrameworkEvent.STARTED:
			LOGGER.info("Handling Framework Restart!!");
			// Add the new BundleContext as a ServletContext attribute replacing the stale BundleContext.
			ServletContext servletContext = ServletContextAware.INSTANCE.getServletContext();
            servletContext.removeAttribute(BundleContext.class.getName());
			BundleContext bundleContext = event.getBundle().getBundleContext();
			servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
			// Sets the new BundleContext in FrameworkProvisioner so that whenever the Server shuts down
			// and in turn OSGi Framework stops, it should not throw [java.lang.IllegalStateException]
			// OSGi Framework should stop gracefully.
			FrameworkProvisioner.INSTANCE.setSystemBundleContext(bundleContext);
			try {
				DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
				this.proxyDispatcherServlet.init();
			} catch (ServletException ex) {
				// Note: What shall we do if ProxyDispatcherServlet initialization failed.
				// Log it as of now, we may need to stop the OSGi Framework, will decide later.
				// Have not seen this yet.
				LOGGER.error("ServletException!!", ex);
			}
			break;
		case FrameworkEvent.STOPPED_UPDATE:
			LOGGER.info("Closing DispatcherServletTracker!!");
			DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
			LOGGER.info("Closing EventDispatcherTracker!!");
			EventDispatcherTrackerSupport.INSTANCE.closeEventDispatcherTracker();
			break;
		default:
			// log it and ignore.
			LOGGER.debug("Ignoring the FrameworkEvent: [{}]", type);
			break;
		}
	}

}
