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
     * This is an instance of Felix {@link org.apache.felix.http.base.internal.EventDispatcher}
     * which implements both {@link HttpSessionListener} and {@link HttpSessionIdListener}
     */
    private volatile EventListener eventListener;

    EventDispatcherTracker(BundleContext context) {
        super(context, EventListener.class);
    }

    @Override
    protected void setService(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    protected void unsetService() {
        this.eventListener = null;
        this.serviceRemoved.set(true);
    }

    @Override
    protected EventListener getTrackedService() {
        return this.eventListener;
    }

    HttpSessionListener getHttpSessionListener() {
        return HttpSessionListener.class.cast(this.getTrackedService());
    }

    HttpSessionIdListener getHttpSessionIdListener() {
        return HttpSessionIdListener.class.cast(this.getTrackedService());
    }

    HttpSessionAttributeListener getHttpSessionAttributeListener() {
        HttpSessionAttributeListener attributeListener = null;
        if (HttpSessionAttributeListener.class.isInstance(this.getTrackedService())) {
            attributeListener = HttpSessionAttributeListener.class.cast(this.getTrackedService());
        }
        return attributeListener;
    }
}
