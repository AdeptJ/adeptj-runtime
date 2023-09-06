/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServlet;

/**
 * Utility for performing operations on OSGi {@link org.osgi.util.tracker.ServiceTracker} instances.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServiceTrackers {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTrackers.class);

    private DispatcherServletTracker dispatcherServletTracker;

    private EventDispatcherTracker eventDispatcherTracker;

    public void openDispatcherServletTracker(BundleContext bundleContext) {
        if (this.dispatcherServletTracker == null) {
            this.dispatcherServletTracker = new DispatcherServletTracker(bundleContext);
            this.dispatcherServletTracker.open();
        }
    }

    public HttpServlet getDispatcherServlet() {
        return this.dispatcherServletTracker.getDispatcherServlet();
    }

    public boolean isDispatcherServletTrackerInitialized() {
        return this.dispatcherServletTracker != null;
    }

    public void closeDispatcherServletTracker() {
        LOGGER.info("Closing DispatcherServletTracker!!");
        this.closeServiceTracker(this.dispatcherServletTracker);
        this.dispatcherServletTracker = null;
    }

    void openEventDispatcherTracker(BundleContext bundleContext) {
        if (this.eventDispatcherTracker == null) {
            LOGGER.info("Opening EventDispatcherTracker!!");
            this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
            this.eventDispatcherTracker.open();
        }
    }

    void reinitializeDispatcherServletTracker(BundleContext bundleContext) {
        this.closeDispatcherServletTracker();
        LOGGER.info("Opening DispatcherServletTracker as OSGi Framework restarted!!");
        this.openDispatcherServletTracker(bundleContext);
    }

    EventDispatcherTracker getEventDispatcherTracker() {
        return this.eventDispatcherTracker;
    }

    void closeEventDispatcherTracker() {
        LOGGER.info("Closing EventDispatcherTracker!!");
        this.closeServiceTracker(this.eventDispatcherTracker);
        this.eventDispatcherTracker = null;
    }

    public static ServiceTrackers getInstance() {
        return INSTANCE;
    }

    private <S, T> void closeServiceTracker(ServiceTracker<S, T> serviceTracker) {
        if (serviceTracker != null) {
            try {
                serviceTracker.close();
            } catch (Exception ex) { // NOSONAR
                LOGGER.error("Exception while closing the ServiceTracker!!", ex);
            }
        }
    }
}
