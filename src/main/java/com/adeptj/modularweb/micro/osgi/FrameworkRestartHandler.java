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

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.BundleContextAware;
import com.adeptj.modularweb.micro.common.ServletContextAware;

/**
 * OSGi FrameworkListener.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkRestartHandler implements FrameworkListener {
	
	/**
	 * Handles OSGi Framework restart, does following on System Bundle STARTED event.
	 * 
	 * 1. Removes the BundleContext from ServletContext attributes.
	 * 2. Gets the new BundleContext from System Bundle and sets that to ServletContext.
	 * 3. Closes the DispatcherServletTracker and open again with new BundleContext.
	 * 4. As the BundleContext is removed and added from ServletContext the corresponding
	 *    BridgeServletContextAttributeListener is fired with events which in turn
	 *    Closes the EventDispatcherTracker and open again with new BundleContext.
	 */
	@Override
	public void frameworkEvent(FrameworkEvent event) {
		Logger logger = LoggerFactory.getLogger(FrameworkRestartHandler.class);
		switch (event.getType()) {
		case FrameworkEvent.STARTED:
			logger.info("Handling OSGi Framework Restart!!");
			// Add the new BundleContext as a ServletContext attribute, remove the stale BundleContext.
			ServletContext servletContext = ServletContextAware.INSTANCE.getServletContext();
            servletContext.removeAttribute(BundleContext.class.getName());
			BundleContext bundleContext = event.getBundle().getBundleContext();
			servletContext.setAttribute(BundleContext.class.getName(), bundleContext);
			// Sets the new BundleContext in BundleContextAware so that whenever the Server shuts down
			// after a restart of OSGi Framework, it should not throw [java.lang.IllegalStateException]
			// while removing this listener by calling removeFrameworkListener method when OSGi Framework stops itself.
			BundleContextAware.INSTANCE.setBundleContext(bundleContext);
			try {
				DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
				// DispatcherServletTrackerSupport already holds a ServletConfig reference
				// from first time when ProxyDispatcherServlet was initialized.
				// Pass a null, which is just fine.
				logger.info("Opening DispatcherServletTracker as OSGi Framework restarted!!");
				DispatcherServletTrackerSupport.INSTANCE.openDispatcherServletTracker(null);
			} catch (Exception ex) {
				// Note: What shall we do if DispatcherServlet initialization failed.
				// Log it as of now, we may need to stop the OSGi Framework, will decide later.
				// Have not seen this yet.
				logger.error("Exception!!", ex);
			}
			break;
		case FrameworkEvent.STOPPED_UPDATE:
			logger.info("Closing DispatcherServletTracker!!");
			DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
			logger.info("Closing EventDispatcherTracker!!");
			EventDispatcherTrackerSupport.INSTANCE.closeEventDispatcherTracker();
			break;
		default:
			// log it and ignore.
			logger.debug("Ignoring the OSGi FrameworkEvent: [{}]", event.getType());
			break;
		}
	}

}
