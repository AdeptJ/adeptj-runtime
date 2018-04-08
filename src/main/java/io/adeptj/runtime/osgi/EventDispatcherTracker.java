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

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.EventDispatcher}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class EventDispatcherTracker extends ServiceTracker<EventListener, EventListener> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherTracker.class);

    private static final String EVENT_DISPATCHER_FILTER = "(http.felix.dispatcher=*)";

    /**
     * This is an instance of Felix EventDispatcher which implements both {@link HttpSessionListener}
     * and {@link HttpSessionIdListener}
     */
    private EventListener eventListener;

    EventDispatcherTracker(BundleContext context) {
        super(context, OSGiUtil.filter(context, EventListener.class, EVENT_DISPATCHER_FILTER), null);
    }

    @Override
    public EventListener addingService(ServiceReference<EventListener> reference) {
        AtomicBoolean eventListenerInitialized = new AtomicBoolean();
        if (this.eventListener == null) {
            LOGGER.info("Adding OSGi Service: [{}]", OSGiUtil.getServiceDesc(reference));
            this.eventListener = super.addingService(reference);
            eventListenerInitialized.set(true);
        }
        return eventListenerInitialized.get() ? this.eventListener : null;
    }

    @Override
    public void removedService(ServiceReference<EventListener> reference, EventListener service) {
        LOGGER.info("Removing OSGi Service: [{}]", OSGiUtil.getServiceDesc(reference));
        super.removedService(reference, service);
    }

    HttpSessionListener getHttpSessionListener() {
        return HttpSessionListener.class.cast(this.eventListener);
    }

    HttpSessionIdListener getHttpSessionIdListener() {
        return HttpSessionIdListener.class.cast(this.eventListener);
    }

    HttpSessionAttributeListener getHttpSessionAttributeListener() {
        HttpSessionAttributeListener attributeListener = null;
        if (HttpSessionAttributeListener.class.isInstance(this.eventListener)) {
            attributeListener = HttpSessionAttributeListener.class.cast(this.eventListener);
        }
        return attributeListener;
    }
}
