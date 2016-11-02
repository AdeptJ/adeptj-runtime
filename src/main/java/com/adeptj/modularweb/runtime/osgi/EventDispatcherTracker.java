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
package com.adeptj.modularweb.runtime.osgi;

import java.util.EventListener;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.common.OSGiUtils;

/**
 * This class is a modified version of FELIX EventDispatcherTracker and rectify the Invalid BundleContext issue.
 * 
 * Issue: When OSGi Framework is being restarted from FELIX web console, original EventDispatcherTracker still holds
 * the stale BundleContext and Framework tries to call the ServiceTracker.addingService method which in turn
 * uses the stale BundleContext for getting the EventDispatcher OSGi service and thus fails with following exception.
 * 
 * <em><b>java.lang.IllegalStateException: Invalid BundleContext</b></em>
 * 
 * To fix the above issue, we close the ServiceTracker in removedService method itself.
 * So that Framework initialize the new EventDispatcherTracker with a fresh BundleContext.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class EventDispatcherTracker extends ServiceTracker<EventListener, EventListener> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherTracker.class);

	private final static String EVENT_DISPATCHER_FILTER = "(http.felix.dispatcher=*)";

	private HttpSessionListener sessionListener;

	private HttpSessionIdListener sessionIdListener;

	private HttpSessionAttributeListener sessionAttributeListener;

	public EventDispatcherTracker(BundleContext context) throws InvalidSyntaxException {
		super(context, OSGiUtils.filter(context, EventListener.class, EVENT_DISPATCHER_FILTER), null);
	}

	@Override
	public EventListener addingService(ServiceReference<EventListener> reference) {
		LOGGER.info("Adding OSGi Service: [{}]", reference.getProperty(Constants.SERVICE_DESCRIPTION));
		EventListener listener = super.addingService(reference);
		if (listener instanceof HttpSessionListener) {
			this.sessionListener = (HttpSessionListener) listener;
		} else if (listener instanceof HttpSessionIdListener) {
			this.sessionIdListener = (HttpSessionIdListener) listener;
		} else if (listener instanceof HttpSessionAttributeListener) {
			this.sessionAttributeListener = (HttpSessionAttributeListener) listener;
		}
		return listener;
	}

	@Override
	public void removedService(ServiceReference<EventListener> reference, EventListener service) {
		LOGGER.info("Removing OSGi Service: [{}]", reference.getProperty(Constants.SERVICE_DESCRIPTION));
		super.removedService(reference, service);
		// NOTE: See class header why ServiceTracker is closed here.
		this.closeTracker();
	}

	private void closeTracker() {
		try {
			this.close();
		} catch (Exception ex) {
			// ignore, anyway Framework is managing it as the EventDispatcher is being removed from service registry.
		}
	}

	public HttpSessionListener getHttpSessionListener() {
		return this.sessionListener;
	}

	public HttpSessionIdListener getHttpSessionIdListener() {
		return this.sessionIdListener;
	}

	public HttpSessionAttributeListener getHttpSessionAttributeListener() {
		return this.sessionAttributeListener;
	}
}
