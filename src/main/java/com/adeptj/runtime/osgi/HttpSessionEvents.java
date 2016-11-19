/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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

import java.util.Optional;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

/**
 * HttpSessionEvents. takes care of HttpSession events.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum HttpSessionEvents {

	SESSION_CREATED,

	SESSION_DESTROYED,

	SESSION_ID_CHANGED,

	SESSION_ATTRIBUTE_ADDED,

	SESSION_ATTRIBUTE_REMOVED,

	SESSION_ATTRIBUTE_REPLACED;

	public static void handleEvent(HttpSessionEvents type, HttpSessionEvent event) {
		switch (type) {
		case SESSION_CREATED:
			optionalSessionListener().ifPresent(listener -> listener.sessionCreated(event));
			break;
		case SESSION_DESTROYED:
			optionalSessionListener().ifPresent(listener -> listener.sessionDestroyed(event));
			break;
		default:
			// NO-OP
			break;
		}
	}

	public static void handleEvent(HttpSessionEvents type, HttpSessionEvent event, String oldSessionId) {
		switch (type) {
		case SESSION_ID_CHANGED:
			optionalSessionIdListener().ifPresent(listener -> listener.sessionIdChanged(event, oldSessionId));
			break;
		default:
			// NO-OP
			break;
		}
	}

	public static void handleEvent(HttpSessionEvents type, HttpSessionBindingEvent bindingEvent) {
		switch (type) {
		case SESSION_ATTRIBUTE_ADDED:
			optionalSessionAttributeListener().ifPresent(listener -> listener.attributeAdded(bindingEvent));
			break;
		case SESSION_ATTRIBUTE_REMOVED:
			optionalSessionAttributeListener().ifPresent(listener -> listener.attributeRemoved(bindingEvent));
			break;
		case SESSION_ATTRIBUTE_REPLACED:
			optionalSessionAttributeListener().ifPresent(listener -> listener.attributeReplaced(bindingEvent));
			break;
		default:
			// NO-OP
			break;
		}
	}

	private static EventDispatcherTracker tracker() {
		return EventDispatcherTrackerSupport.INSTANCE.getEventDispatcherTracker();
	}

	private static Optional<HttpSessionListener> optionalSessionListener() {
		return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionListener());
	}

	private static Optional<HttpSessionIdListener> optionalSessionIdListener() {
		return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionIdListener());
	}

	private static Optional<HttpSessionAttributeListener> optionalSessionAttributeListener() {
		return Optional.ofNullable(tracker() == null ? null : tracker().getHttpSessionAttributeListener());
	}
}