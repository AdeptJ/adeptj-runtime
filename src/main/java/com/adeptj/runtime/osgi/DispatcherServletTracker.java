/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.osgi;

import com.adeptj.runtime.common.OSGiUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

/**
 * OSGi ServiceTracker for FELIX DispatcherServlet.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends ServiceTracker<HttpServlet, HttpServlet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletTracker.class);

    private static final String DISPATCHER_SERVLET_FILTER = "(http.felix.dispatcher=*)";

    private HttpServlet dispatcherServlet;

    public DispatcherServletTracker(BundleContext context) {
        super(context, OSGiUtils.filter(context, HttpServlet.class, DISPATCHER_SERVLET_FILTER), null);
    }

    @Override
    public HttpServlet addingService(ServiceReference<HttpServlet> reference) {
        HttpServlet httpServlet = null;
        try {
            httpServlet = super.addingService(reference);
            LOGGER.info("Adding OSGi Service: [{}]", reference.getProperty(Constants.SERVICE_DESCRIPTION));
            this.handleDispatcherServlet(httpServlet);
        } catch (Exception ex) { // NOSONAR
            // This might be due to the OSGi framework restart from Felix WebConsole.
            LOGGER.error("Exception adding Felix DispatcherServlet OSGi Service!!", ex);
        }
        return httpServlet;
    }

    @Override
    public void removedService(ServiceReference<HttpServlet> reference, HttpServlet service) {
        LOGGER.info("Removing OSGi Service: [{}]", reference.getProperty(Constants.SERVICE_DESCRIPTION));
        // Passing null so that DispatcherServlet.core() won't be called again.
        this.handleDispatcherServlet(null);
        super.removedService(reference, service);
        /*
         * Note: Since the DispatcherServlet has already been removed therefore close this ServiceTracker too.
         * Otherwise this will cause [java.lang.IllegalStateException: Invalid BundleContext] on Framework restart.
         * When Framework is being restarted from Web console then tracker is still open with stale BundleContext.
         * Now with the Framework restart DispatcherServlet OSGi service becomes available and Framework calls addingService method.
         * Which further tries to get DispatcherServlet OSGi service using the stale BundleContext
         * from which this ServiceTracker was created and causes the [java.lang.IllegalStateException: Invalid BundleContext].
         * Since a Framework restart creates a new BundleContext and this tracker will again be opened with
         * the new BundleContext therefore closing it here make sense.
         * 
         * Ignore exceptions, anyway Framework is managing it as the DispatcherServlet is being removed from service registry.
        */
        ServiceTrackers.INSTANCE.closeQuietly(this);
    }

    HttpServlet getDispatcherServlet() {
        return this.dispatcherServlet;
    }

    private void handleDispatcherServlet(HttpServlet dispatcherServlet) {
        this.destroyDispatcherServlet();
        this.dispatcherServlet = dispatcherServlet;
        this.initDispatcherServlet();
    }

    private void destroyDispatcherServlet() {
        if (this.dispatcherServlet != null) {
            LOGGER.info("Destroying Felix DispatcherServlet: [{}]", this.dispatcherServlet);
            this.dispatcherServlet.destroy();
            // Set dispatcherServlet as null, don't want to call dispatcherServlet.core with this reference.
            this.dispatcherServlet = null;
        }
    }

    private void initDispatcherServlet() {
        if (this.dispatcherServlet != null) {
            try {
                LOGGER.info("Initializing Felix DispatcherServlet!!");
                this.dispatcherServlet.init(DispatcherServletTrackerSupport.INSTANCE.getServletConfig());
                LOGGER.info("Felix DispatcherServlet: [{}]", this.dispatcherServlet);
            } catch (Exception ex) { // NOSONAR
                LOGGER.error("Failed to initialize Felix DispatcherServlet!!", ex);
            }
        }
    }
}