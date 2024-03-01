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

import com.adeptj.runtime.kernel.util.Times;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    SESSION_DESTROYED;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionEvents.class);

    // <<---------- HttpSessionListener ---------->>

    public static void handleHttpSessionEvent(HttpSessionEvents type, HttpSessionEvent event) {
        if (type == SESSION_CREATED) {
            logSessionCreated(event);
            sessionListener().ifPresent(listener -> listener.sessionCreated(event));
        } else if (type == SESSION_DESTROYED) {
            logSessionDestroyed(event);
            sessionListener().ifPresent(listener -> listener.sessionDestroyed(event));
        }
    }

    // <<---------- HttpSessionIdListener ---------->>

    public static void handleSessionIdChangedEvent(HttpSessionEvent event, String oldSessionId) {
        sessionIdListener().ifPresent(listener -> listener.sessionIdChanged(event, oldSessionId));
    }

    private static void logSessionCreated(HttpSessionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created HttpSession with id: [{}], @Time: [{}]",
                    event.getSession().getId(),
                    Date.from(Instant.ofEpochMilli(event.getSession().getCreationTime())));
        }
    }

    private static void logSessionDestroyed(HttpSessionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Destroyed HttpSession with id: [{}], active for: [{}] seconds.",
                    event.getSession().getId(),
                    Times.elapsedSeconds(event.getSession().getCreationTime()));
        }
    }

    private static EventDispatcherTracker tracker() {
        return ServiceTrackers.getInstance().getEventDispatcherTracker();
    }

    private static Optional<HttpSessionListener> sessionListener() {
        EventDispatcherTracker tracker = tracker();
        return Optional.ofNullable(tracker == null ? null : tracker.getHttpSessionListener());
    }

    private static Optional<HttpSessionIdListener> sessionIdListener() {
        EventDispatcherTracker tracker = tracker();
        return Optional.ofNullable(tracker == null ? null : tracker.getHttpSessionIdListener());
    }
}