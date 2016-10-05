/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
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

	private EventDispatcherTracker eventDispatcherTracker;

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		String evtName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", evtName);
		if (BundleContext.class.getName().equals(evtName)) {
			this.startTracker(ServletContextAware.INSTANCE.getAttr(evtName, BundleContext.class));
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		String evtName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", evtName);
		if (BundleContext.class.getName().equals(evtName)) {
			this.stopTracker();
		}
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		String evtName = event.getName();
		LOGGER.debug("Adding context attribute: [{}]", evtName);
		if (BundleContext.class.getName().equals(evtName)) {
			this.stopTracker();
			this.startTracker(ServletContextAware.INSTANCE.getAttr(evtName, BundleContext.class));
		}
	}
	
	private void startTracker(BundleContext bundleContext) {
		try {
			this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
			LOGGER.info("Opening EventDispatcherTracker!!");
			this.eventDispatcherTracker.open();
			EventDispatcherTrackerSupport.INSTANCE.setEventDispatcherTracker(this.eventDispatcherTracker);
		} catch (InvalidSyntaxException ise) {
			// not expected for our simple filter, just log it.
			LOGGER.error("InvalidSyntaxException!!", ise);
		}
	}

	private void stopTracker() {
		if (this.eventDispatcherTracker != null) {
			LOGGER.info("BundleContext attribute either removed or replaced from ServletContext, closing EventDispatcherTracker!!");
			this.eventDispatcherTracker.close();
			this.eventDispatcherTracker = null;
			EventDispatcherTrackerSupport.INSTANCE.setEventDispatcherTracker(this.eventDispatcherTracker);
		}
	}

}
