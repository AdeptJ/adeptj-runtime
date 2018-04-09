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

import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.util.EventListener;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.EventDispatcher}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class EventDispatcherTracker extends BridgeServiceTracker<EventListener> {

    /**
     * The Felix {@link org.apache.felix.http.base.internal.EventDispatcher}
     * which implements both {@link HttpSessionListener} and {@link HttpSessionIdListener}
     */
    private volatile EventListener eventListener;

    /**
     * Create the {@link org.osgi.util.tracker.ServiceTracker} for {@link EventListener}
     *
     * @param context the {@link BundleContext}
     */
    EventDispatcherTracker(BundleContext context) {
        super(context, EventListener.class);
    }

    /**
     * Initializes the {@link EventListener}
     *
     * @param trackedService the tracked service instance.
     */
    @Override
    protected void setup(EventListener trackedService) {
        this.eventListener = trackedService;
    }

    /**
     * Sets the {@link EventListener} to null.
     */
    @Override
    protected void cleanup() {
        this.eventListener = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EventListener getServiceInstance() {
        return this.eventListener;
    }

    HttpSessionListener getHttpSessionListener() {
        return HttpSessionListener.class.cast(this.getServiceInstance());
    }

    HttpSessionIdListener getHttpSessionIdListener() {
        return HttpSessionIdListener.class.cast(this.getServiceInstance());
    }

    HttpSessionAttributeListener getHttpSessionAttributeListener() {
        HttpSessionAttributeListener attributeListener = null;
        if (HttpSessionAttributeListener.class.isInstance(this.getServiceInstance())) {
            attributeListener = HttpSessionAttributeListener.class.cast(this.getServiceInstance());
        }
        return attributeListener;
    }
}
