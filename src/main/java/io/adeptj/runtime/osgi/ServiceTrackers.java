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
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ServiceTrackers. Utility for performing operations on OSGi ServiceTracker instances.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ServiceTrackers {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTrackers.class);

    private volatile boolean dispatcherServletInitialized;

    private DispatcherServletTracker servletTracker;

    private EventDispatcherTracker eventDispatcherTracker;

    private Map<String, ServiceTracker<Object, Object>> trackers = new HashMap<>();

    public void openDispatcherServletTracker() {
        if (!this.dispatcherServletInitialized && this.servletTracker == null) {
            BundleContext bundleContext = BundleContextHolder.INSTANCE.getBundleContext();
            DispatcherServletTracker tracker = new DispatcherServletTracker(bundleContext);
            tracker.open();
            this.servletTracker = tracker;
            this.dispatcherServletInitialized = true;
        }
    }

    public HttpServlet getDispatcherServlet() {
        return this.servletTracker == null ? null : this.servletTracker.getDispatcherServlet();
    }

    public void closeDispatcherServletTracker() {
        this.dispatcherServletInitialized = false;
        if (this.servletTracker != null && !this.servletTracker.isEmpty()) {
            this.servletTracker.close();
        }
        this.servletTracker = null;
    }

    protected EventDispatcherTracker getEventDispatcherTracker() {
        return this.eventDispatcherTracker;
    }

    protected void openEventDispatcherTracker(BundleContext bundleContext) {
        if (this.eventDispatcherTracker == null) {
            LOGGER.info("Opening EventDispatcherTracker!!");
            this.eventDispatcherTracker = new EventDispatcherTracker(bundleContext);
            this.eventDispatcherTracker.open();
        }
    }

    protected void closeEventDispatcherTracker() {
        if (this.eventDispatcherTracker != null && !this.eventDispatcherTracker.isEmpty()) {
            this.eventDispatcherTracker.close();
            LOGGER.info("EventDispatcherTracker Closed!!");
        }
        this.eventDispatcherTracker = null;
    }

    public void track(Class<? extends ServiceTracker<Object, Object>> klazz, ServiceTracker<Object, Object> tracker) {
        this.trackers.put(klazz.getName(), tracker);
        tracker.open();
    }

    public void close(Class<? extends ServiceTracker<Object, Object>> trackerClass) {
        Optional.ofNullable(this.trackers.remove(trackerClass.getName())).ifPresent(ServiceTracker::close);
    }

    public void closeAll() {
        this.trackers.forEach((trackerClass, tracker) -> tracker.close());
    }

    public ServiceTracker<Object, Object> getTracker(Class<? extends ServiceTracker<Object, Object>> trackerClass) {
        return this.trackers.get(trackerClass.getName());
    }

    public void close(ServiceTracker<Object, Object> tracker) {
        tracker.close();
    }

    public void closeQuietly(ServiceTracker<Object, Object> tracker) {
        try {
            tracker.close();
        } catch (Exception ex) { // NOSONAR
            // Ignore, anyway Framework is managing it as the Tracked service is being removed from service registry.
        }
    }

    public void closeQuietly(DispatcherServletTracker dispatcherServletTracker) {
        try {
            dispatcherServletTracker.close();
        } catch (Exception ex) { // NOSONAR
            // Ignore, anyway Framework is managing it as the Tracked service is being removed from service registry.
        }
    }

    public void closeQuietly(EventDispatcherTracker eventDispatcherTracker) {
        try {
            eventDispatcherTracker.close();
        } catch (Exception ex) { // NOSONAR
            // Ignore, anyway Framework is managing it as the Tracked service is being removed from service registry.
        }
    }
}
