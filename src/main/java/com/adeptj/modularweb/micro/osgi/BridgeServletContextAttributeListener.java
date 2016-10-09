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

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;

/**
 * BridgeServletContextAttributeListener.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class BridgeServletContextAttributeListener implements ServletContextAttributeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServletContextAttributeListener.class);

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		String attrName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", attrName);
		if (this.isBundleContext(attrName)) {
			this.openEventDispatcherTracker(ServletContextAware.INSTANCE.getAttr(attrName, BundleContext.class));
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		String attrName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", attrName);
		if (this.isBundleContext(attrName)) {
			this.closeEventDispatcherTracker();
		}
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		String attrName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", attrName);
		if (this.isBundleContext(attrName)) {
			this.closeEventDispatcherTracker();
			this.openEventDispatcherTracker(ServletContextAware.INSTANCE.getAttr(attrName, BundleContext.class));
		}
	}
	
	private boolean isBundleContext(String attrName) {
		return BundleContext.class.getName().equals(attrName);
	}
	
	private void openEventDispatcherTracker(BundleContext bundleContext) {
		try {
			LOGGER.info("Opening EventDispatcherTracker!!");
			EventDispatcherTrackerSupport.INSTANCE.openEventDispatcherTracker(bundleContext);
		} catch (InvalidSyntaxException ise) {
			// not expected for our simple filter, just log it.
			LOGGER.error("InvalidSyntaxException!!", ise);
		}
	}

	private void closeEventDispatcherTracker() {
		LOGGER.info(
				"BundleContext attribute either removed or replaced from ServletContext, closing EventDispatcherTracker!!");
		EventDispatcherTrackerSupport.INSTANCE.closeEventDispatcherTracker();
	}

}
