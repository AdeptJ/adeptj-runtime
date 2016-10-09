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

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for EventDispatcherTracker.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum EventDispatcherTrackerSupport {

	INSTANCE;

	private volatile EventDispatcherTracker eventDispatcherTracker;

	protected EventDispatcherTracker getEventDispatcherTracker() {
		return this.eventDispatcherTracker;
	}

	protected void openEventDispatcherTracker(BundleContext bundleContext) {
		if (this.eventDispatcherTracker == null) {
			Logger logger = LoggerFactory.getLogger(EventDispatcherTrackerSupport.class);
			try {
				logger.info("Opening EventDispatcherTracker!!");
				this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
				this.eventDispatcherTracker.open();
			} catch (InvalidSyntaxException ise) {
				// Not possible for our simple filter but log it anyway and re-throw.
				logger.error("InvalidSyntaxException!!", ise);
			}
		}
	}

	protected void closeEventDispatcherTracker() {
		if (this.eventDispatcherTracker != null) {
			this.eventDispatcherTracker.close();
			this.eventDispatcherTracker = null;
			LoggerFactory.getLogger(EventDispatcherTrackerSupport.class).info("EventDispatcherTracker Closed!!");
		}
	}
}
