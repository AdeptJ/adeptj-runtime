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
package com.adeptj.modularweb.micro.bootstrap.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

/**
 * OSGi ServiceTracker for Felix DispatcherServlet.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends ServiceTracker<HttpServlet, HttpServlet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletTracker.class);

    private final static String DEFAULT_FILTER = "(http.felix.dispatcher=org.apache.felix.http.base.internal.DispatcherServlet)";

    private final ServletConfig config;

    private HttpServlet dispatcher;

    public DispatcherServletTracker(BundleContext context, ServletConfig config) throws InvalidSyntaxException {
        super(context, DispatcherServletTracker.createFilter(context), null);
        this.config = config;
    }

    public HttpServlet getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public HttpServlet addingService(ServiceReference<HttpServlet> reference) {
        HttpServlet dispatcherServlet = null;
        try {
            dispatcherServlet = super.addingService(reference);
            LOGGER.info("Adding Service: [{}]", dispatcherServlet);
            this.handleDispatcher(dispatcherServlet);
        } catch (Exception ex) {
            // This might be due to the OSGi framework restart from Felix WebConsole.
            LOGGER.error("addingService failed!!", ex);
        }
        return dispatcherServlet;
    }

    @Override
    public void removedService(ServiceReference<HttpServlet> reference, HttpServlet service) {
        LOGGER.info("Removing Service: [{}]", service);
        // Passing null so that DispatcherServlet.init() won't be called again.
        this.handleDispatcher(null);
        super.removedService(reference, service);
        /*
         * Note: Since the DispatcherServlet has already been removed therefore close this ServiceTracker too.
         * Otherwise this will cause [java.lang.IllegalStateException: Invalid BundleContext] on Framework restart.
         * When Framework is being restarted from Web console then tracker is still open with stale BundleContext.
         * Now with the Framework restart DispatcherServlet OSGi service becomes available and Framework calls addingService method.
         * Which further tries to get DispatcherServlet OSGi service using the stale the BundleContext
         * from which this ServiceTracker was created and causes the [java.lang.IllegalStateException: Invalid BundleContext].
         * Since a Framework restart creates a new BundleContext and this tracker will again be opened with
         * the new BundleContext therefore closing it here make sense.
        */
        try {
            this.close();
        } catch (Exception ex) {
            // ignore, anyway Framework is managing it as the DispatcherServlet is being removed from service registry.
        }
    }

    private void handleDispatcher(HttpServlet dispatcher) {
        this.destroyDispatcher();
        this.dispatcher = dispatcher;
        this.initDispatcher();
    }

    private void destroyDispatcher() {
        if (this.dispatcher == null) {
            return;
        }
        LOGGER.info("Destroying DispatcherServlet: [{}]", this.dispatcher);
        this.dispatcher.destroy();
        this.dispatcher = null;
    }

    private void initDispatcher() {
        if (this.dispatcher == null) {
            return;
        }
        try {
            LOGGER.info("Initializing DispatcherServlet!!");
            this.dispatcher.init(this.config);
            LOGGER.info("DispatcherServlet Initialized: [{}]", this.dispatcher);
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize dispatcher!!", ex);
        }
    }

    private static Filter createFilter(BundleContext context) throws InvalidSyntaxException {
        StringBuilder filterExpr = new StringBuilder();
        filterExpr.append("(&(").append(Constants.OBJECTCLASS).append("=");
        filterExpr.append(HttpServlet.class.getName()).append(")");
        filterExpr.append(DEFAULT_FILTER).append(")");
        String filter = filterExpr.toString();
        LOGGER.debug("ServiceTracker Filter: [{}]", filter);
        return context.createFilter(filter);
    }
}
