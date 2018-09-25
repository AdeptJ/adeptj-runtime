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

import io.adeptj.runtime.common.OSGiUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Boilerplate for {@link DispatcherServletTracker} and {@link EventDispatcherTracker}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public abstract class BridgeServiceTracker<T> extends ServiceTracker<T, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServiceTracker.class);

    private static final String DISPATCHER_SERVICE_FILTER = "(http.felix.dispatcher=*)";

    /**
     * This is to prevent {@link IllegalStateException} : Invalid {@link BundleContext} when service is removed and
     * then added again on framework restart using the stale {@link BundleContext}.
     * <p>
     * This is registered again using the fresh {@link BundleContext} obtained via {@link FrameworkLifecycleListener}
     */
    private final AtomicBoolean serviceRemoved;

    BridgeServiceTracker(BundleContext context, Class<T> objectClass) {
        super(context, OSGiUtil.filter(context, objectClass, DISPATCHER_SERVICE_FILTER), null);
        this.serviceRemoved = new AtomicBoolean();
    }

    /**
     * Called by {@link ServiceTracker} when the service to look becomes available.
     *
     * @param reference the tracked service reference.
     * @return tracked service instance.
     */
    @Override
    public T addingService(ServiceReference<T> reference) {
        T service = null;
        if (!this.serviceRemoved.get()) {
            LOGGER.info("Adding OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
            service = this.addingService(super.addingService(reference));
        }
        return this.serviceRemoved.get() ? null : service;
    }

    /**
     * Called by {@link ServiceTracker} when the service to look removed from OSGi service registry.
     *
     * @param reference the tracked service reference.
     * @param service   tracked service instance.
     */
    @Override
    public void removedService(ServiceReference<T> reference, T service) {
        LOGGER.info("Removing OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
        try {
            this.removedService(service);
        } catch (Exception ex) { // NOSONAR
            LOGGER.error(ex.getMessage(), ex);
        }
        this.serviceRemoved.set(true);
        super.removedService(reference, service);
    }

    /**
     * Perform any setup tasks with the tracked service instance.
     *
     * @param service the tracked service instance.
     * @return The fully initialized service instance or null if service need not to be tracked.
     */
    protected abstract T addingService(T service);

    /**
     * Perform any cleanup tasks on the tracked service instance.
     */
    protected abstract void removedService(T service);
}
