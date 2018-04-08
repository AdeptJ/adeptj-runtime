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

package io.adeptj.runtime.osgi;

import io.adeptj.runtime.common.BundleContextHolder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.Optional;

/**
 * ServiceTrackers. Utility for performing operations on OSGi {@link org.osgi.util.tracker.ServiceTracker} instances.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServiceTrackers {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTrackers.class);

    private volatile DispatcherServletTracker dispatcherServletTracker;

    private volatile EventDispatcherTracker eventDispatcherTracker;

    public void openDispatcherServletTracker() {
        if (this.dispatcherServletTracker == null) {
            this.dispatcherServletTracker = new DispatcherServletTracker(BundleContextHolder.INSTANCE.getBundleContext());
            this.dispatcherServletTracker.open();
        }
    }

    public HttpServlet getDispatcherServlet() {
        return this.dispatcherServletTracker == null ? null : this.dispatcherServletTracker.getDispatcherServlet();
    }

    public void closeDispatcherServletTracker() {
        LOGGER.info("Closing DispatcherServletTracker!!");
        Optional.ofNullable(this.dispatcherServletTracker).ifPresent(DispatcherServletTracker::close);
        this.dispatcherServletTracker = null;
    }

    protected void openEventDispatcherTracker(BundleContext bundleContext) {
        if (this.eventDispatcherTracker == null) {
            LOGGER.info("Opening EventDispatcherTracker!!");
            this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
            this.eventDispatcherTracker.open();
        }
    }

    protected EventDispatcherTracker getEventDispatcherTracker() {
        return this.eventDispatcherTracker;
    }

    protected void closeEventDispatcherTracker() {
        LOGGER.info("Closing EventDispatcherTracker!!");
        Optional.ofNullable(this.eventDispatcherTracker).ifPresent(EventDispatcherTracker::close);
        this.eventDispatcherTracker = null;
    }
}
