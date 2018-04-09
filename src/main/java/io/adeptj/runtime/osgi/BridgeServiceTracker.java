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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Boilerplate for {@link DispatcherServletTracker} and {@link EventDispatcherTracker}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public abstract class BridgeServiceTracker<T> extends ServiceTracker<T, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServiceTracker.class);

    private static final String DEFAULT_FILTER = "(http.felix.dispatcher=*)";

    /**
     * Stores service removal status.
     */
    private final AtomicBoolean serviceRemoved;

    BridgeServiceTracker(BundleContext context, Class<T> objectClass) {
        super(context, OSGiUtil.filter(context, objectClass, DEFAULT_FILTER), null);
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
        if (!this.serviceRemoved.get() && this.getServiceInstance() == null) {
            LOGGER.info("Adding OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
            this.setup(super.addingService(reference));
        }
        return this.serviceRemoved.get() ? null : this.getServiceInstance();
    }

    /**
     * Called by {@link ServiceTracker} when the service to look removed from OSGi service registry.
     *
     * @param reference the tracked service reference.
     * @param service   tracked service instance.
     */
    @Override
    public void removedService(ServiceReference<T> reference, T service) {
        if (Objects.equals(service, this.getServiceInstance())) {
            LOGGER.info("Removing OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
            try {
                this.cleanup();
            } catch (Exception ex) { // NOSONAR
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        this.serviceRemoved.set(true);
        super.removedService(reference, service);
    }

    /**
     * Perform any setup tasks with the tracked service instance.
     *
     * @param trackedService the tracked service instance.
     */
    protected abstract void setup(T trackedService);

    /**
     * Perform any cleanup tasks on the tracked service instance.
     */
    protected abstract void cleanup();

    /**
     * Returns the fully initialized tracked service instance.
     *
     * @return the fully initialized tracked service instance.
     */
    protected abstract T getServiceInstance();
}
