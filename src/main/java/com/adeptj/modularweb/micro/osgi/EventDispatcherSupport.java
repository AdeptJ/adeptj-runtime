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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;

/**
 * This is a support class for FELIX EventDispatcher. FELIX ProxyServletContextListener class is doing what this class does here. 
 * But UNDERTOW is throwing below mentioned exception while adding listeners in ProxyServletContextListener.contextInitialized method.
 * 
 * <em><b>(java.lang.UnsupportedOperationException: UT010042: This method cannot be called from a SERVLET context listener that has been added programmatically)</b></em>
 * 
 * This does not seem to be in compliance with SERVLET specification as same code works in WILDFLY server.
 * 
 * So using this as a workaround for the time being.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum EventDispatcherSupport {

	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherSupport.class);

	private volatile EventDispatcherTracker eventDispatcherTracker;

	/**
	 * Adds the following to ServletContext.
	 * 
	 * HttpSessionListener
	 * 
	 * HttpSessionIdListener
	 * 
	 * HttpSessionAttributeListener
	 * 
	 * ServletContextAttributeListener
	 * 
	 */
	public void initListeners(ServletContext servletContext) {
		// add all required listeners
		this.addHttpSessionListener(servletContext);
		this.addHttpSessionIdListener(servletContext);
		this.addHttpSessionAttributeListener(servletContext);
		this.addServletContextAttributeListener(servletContext);

	}

	private void addServletContextAttributeListener(ServletContext servletContext) {
		servletContext.addListener(new ServletContextAttributeListener() {

			@Override
			public void attributeAdded(final ServletContextAttributeEvent event) {
				LOGGER.debug("Adding context attribute: [{}]", event.getName());
				if (event.getName().equals(BundleContext.class.getName())) {
					startTracker(ServletContextAware.INSTANCE.getAttr(event.getName(), BundleContext.class));
				}
			}

			@Override
			public void attributeRemoved(final ServletContextAttributeEvent event) {
				LOGGER.debug("Removing context attribute: [{}]", event.getName());
				if (event.getName().equals(BundleContext.class.getName())) {
					stopTracker();
				}
			}

			@Override
			public void attributeReplaced(final ServletContextAttributeEvent event) {
				LOGGER.debug("Replacing context attribute: [{}]", event.getName());
				if (event.getName().equals(BundleContext.class.getName())) {
					stopTracker();
					startTracker(ServletContextAware.INSTANCE.getAttr(event.getName(), BundleContext.class));
				}
			}
		});
	}

	private void addHttpSessionAttributeListener(ServletContext servletContext) {
		servletContext.addListener(new HttpSessionAttributeListener() {

			private HttpSessionAttributeListener getHttpSessionAttributeListener() {
				final EventDispatcherTracker tracker = eventDispatcherTracker;
				if (tracker != null) {
					return tracker.getHttpSessionAttributeListener();
				}
				return null;
			}

			@Override
			public void attributeAdded(final HttpSessionBindingEvent se) {
				final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
				if (attributeDispatcher != null) {
					attributeDispatcher.attributeAdded(se);
				}
			}

			@Override
			public void attributeRemoved(final HttpSessionBindingEvent se) {
				final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
				if (attributeDispatcher != null) {
					attributeDispatcher.attributeRemoved(se);
				}
			}

			@Override
			public void attributeReplaced(final HttpSessionBindingEvent se) {
				final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
				if (attributeDispatcher != null) {
					attributeDispatcher.attributeReplaced(se);
				}
			}
		});
	}

	private void addHttpSessionIdListener(ServletContext servletContext) {
		servletContext.addListener(new HttpSessionIdListener() {

			private HttpSessionIdListener getHttpSessionIdListener() {
				final EventDispatcherTracker tracker = eventDispatcherTracker;
				if (tracker != null) {
					return tracker.getHttpSessionIdListener();
				}
				return null;
			}

			@Override
			public void sessionIdChanged(final HttpSessionEvent event, final String oldSessionId) {
				final HttpSessionIdListener sessionIdDispatcher = getHttpSessionIdListener();
				if (sessionIdDispatcher != null) {
					sessionIdDispatcher.sessionIdChanged(event, oldSessionId);
				}
			}
		});
	}

	private void addHttpSessionListener(ServletContext servletContext) {
		servletContext.addListener(new HttpSessionListener() {

			private HttpSessionListener getHttpSessionListener() {
				final EventDispatcherTracker tracker = eventDispatcherTracker;
				if (tracker != null) {
					return tracker.getHttpSessionListener();
				}
				return null;
			}

			@Override
			public void sessionCreated(final HttpSessionEvent se) {
				final HttpSessionListener sessionDispatcher = getHttpSessionListener();
				if (sessionDispatcher != null) {
					sessionDispatcher.sessionCreated(se);
				}
			}

			@Override
			public void sessionDestroyed(final HttpSessionEvent se) {
				final HttpSessionListener sessionDispatcher = getHttpSessionListener();
				if (sessionDispatcher != null) {
					sessionDispatcher.sessionDestroyed(se);
				}
			}
		});
	}

	public void startTracker(final BundleContext bundleContext) {
		try {
			this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
			LOGGER.info("Opening EventDispatcherTracker!!");
			this.eventDispatcherTracker.open();
		} catch (final InvalidSyntaxException ise) {
			// not expected for our simple filter
			LOGGER.error("InvalidSyntaxException!!", ise);
		}
	}

	public void stopTracker() {
		if (this.eventDispatcherTracker != null) {
			this.eventDispatcherTracker.close();
			LOGGER.info("Closing EventDispatcherTracker!!");
			this.eventDispatcherTracker = null;
		}
	}
}
