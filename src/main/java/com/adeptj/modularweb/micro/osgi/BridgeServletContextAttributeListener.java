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

/**
 * BridgeServletContextAttributeListener.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class BridgeServletContextAttributeListener implements ServletContextAttributeListener {

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		if (this.isBundleContext(event.getName())) {
			EventDispatcherTrackerSupport.INSTANCE.openEventDispatcherTracker((BundleContext) event.getValue());
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		if (this.isBundleContext(event.getName())) {
			EventDispatcherTrackerSupport.INSTANCE.closeEventDispatcherTracker();
		}
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		// Does nothing as of now.
	}

	private boolean isBundleContext(String attrName) {
		return BundleContext.class.getName().equals(attrName);
	}
}
