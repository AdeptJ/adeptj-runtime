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
     * Instance of Felix {@link org.apache.felix.http.base.internal.EventDispatcher} which implements
     * the {@link HttpSessionListener}
     */
    private HttpSessionListener sessionListener;

    /**
     * Instance of Felix {@link org.apache.felix.http.base.internal.EventDispatcher} which implements
     * the {@link HttpSessionIdListener}
     */
    private HttpSessionIdListener sessionIdListener;

    /**
     * Just in case someone implements {@link HttpSessionAttributeListener} OSGi service, track that as well.
     */
    private HttpSessionAttributeListener sessionAttributeListener;

    /**
     * Create the {@link org.osgi.util.tracker.ServiceTracker} for {@link EventListener}
     *
     * @param context the {@link BundleContext}
     */
    EventDispatcherTracker(BundleContext context) {
        super(context, EventListener.class);
    }

    /**
     * Initializes {@link HttpSessionListener}, {@link HttpSessionIdListener} and {@link HttpSessionAttributeListener}
     *
     * @param eventListener the tracked eventListener instance.
     */
    @Override
    protected EventListener addingService(EventListener eventListener) {
        if (eventListener instanceof HttpSessionListener) {
            this.sessionListener = (HttpSessionListener) eventListener;
        }
        if (eventListener instanceof HttpSessionIdListener) {
            this.sessionIdListener = (HttpSessionIdListener) eventListener;
        }
        if (eventListener instanceof HttpSessionAttributeListener) {
            this.sessionAttributeListener = (HttpSessionAttributeListener) eventListener;
        }
        return eventListener;
    }

    /**
     * Sets the {@link EventListener} instances to null.
     *
     * @param eventListener the tracked eventListener instance.
     */
    @Override
    protected void removedService(EventListener eventListener) {
        if (eventListener instanceof HttpSessionListener) {
            this.sessionListener = null;
        }
        if (eventListener instanceof HttpSessionIdListener) {
            this.sessionIdListener = null;
        }
        if (eventListener instanceof HttpSessionAttributeListener) {
            this.sessionAttributeListener = null;
        }
    }

    HttpSessionListener getHttpSessionListener() {
        return this.sessionListener;
    }

    HttpSessionIdListener getHttpSessionIdListener() {
        return this.sessionIdListener;
    }

    HttpSessionAttributeListener getHttpSessionAttributeListener() {
        return this.sessionAttributeListener;
    }
}
