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

import io.adeptj.runtime.common.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * HttpSessionEvents. Propagates the HttpSession events to OSGi registered EventListener(s).
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum HttpSessionEvents {

    SESSION_CREATED,

    SESSION_DESTROYED,

    SESSION_ATTRIBUTE_ADDED,

    SESSION_ATTRIBUTE_REMOVED,

    SESSION_ATTRIBUTE_REPLACED;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionEvents.class);

    public static void handleEvent(HttpSessionEvents type, HttpSessionEvent event) {
        switch (type) {
            case SESSION_CREATED:
                logSessionCreated(event);
                sessionListener().ifPresent(listener -> listener.sessionCreated(event));
                break;
            case SESSION_DESTROYED:
                logSessionDestroyed(event);
                sessionListener().ifPresent(listener -> listener.sessionDestroyed(event));
                break;
            default:
                // NO-OP
                break;
        }
    }

    public static void handleSessionIdChangedEvent(HttpSessionEvent event, String oldSessionId) {
        sessionIdListener().ifPresent(listener -> listener.sessionIdChanged(event, oldSessionId));
    }

    public static void handleEvent(HttpSessionEvents type, HttpSessionBindingEvent bindingEvent) {
        switch (type) {
            case SESSION_ATTRIBUTE_ADDED:
                sessionAttributeListener().ifPresent(listener -> listener.attributeAdded(bindingEvent));
                break;
            case SESSION_ATTRIBUTE_REMOVED:
                sessionAttributeListener().ifPresent(listener -> listener.attributeRemoved(bindingEvent));
                break;
            case SESSION_ATTRIBUTE_REPLACED:
                sessionAttributeListener().ifPresent(listener -> listener.attributeReplaced(bindingEvent));
                break;
            default:
                // NO-OP
                break;
        }
    }

    private static void logSessionCreated(HttpSessionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created HttpSession with id: [{}], @Time: [{}]", event.getSession().getId(),
                    Date.from(Instant.ofEpochMilli(event.getSession().getCreationTime())));
        }
    }

    private static void logSessionDestroyed(HttpSessionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Destroyed HttpSession with id: [{}], active for: [{}] seconds.", event.getSession().getId(),
                    Times.elapsedSeconds(event.getSession().getCreationTime()));
        }
    }

    private static EventDispatcherTracker tracker() {
        return ServiceTrackers.INSTANCE.getEventDispatcherTracker();
    }

    private static Optional<HttpSessionListener> sessionListener() {
        return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionListener());
    }

    private static Optional<HttpSessionIdListener> sessionIdListener() {
        return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionIdListener());
    }

    private static Optional<HttpSessionAttributeListener> sessionAttributeListener() {
        return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionAttributeListener());
    }
}