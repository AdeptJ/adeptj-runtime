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

    AtomicBoolean serviceRemoved;

    BridgeServiceTracker(BundleContext context, Class<T> objectClass) {
        super(context, OSGiUtil.filter(context, objectClass, DEFAULT_FILTER), null);
        this.serviceRemoved = new AtomicBoolean();
    }

    @Override
    public T addingService(ServiceReference<T> reference) {
        if (!this.serviceRemoved.get()) {
            LOGGER.info("Adding OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
            this.setService(super.addingService(reference));
        }
        return this.serviceRemoved.get() ? null : this.getTrackedService();
    }

    @Override
    public void removedService(ServiceReference<T> reference, T service) {
        if (Objects.equals(service, this.getTrackedService())) {
            LOGGER.info("Removing OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
            this.unsetService();
        }
        super.removedService(reference, service);
    }

    protected abstract void setService(T service);

    protected abstract void unsetService();

    protected abstract T getTrackedService();
}
