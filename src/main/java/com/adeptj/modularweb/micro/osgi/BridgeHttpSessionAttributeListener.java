/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
 */
package com.adeptj.modularweb.micro.osgi;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * BridgeHttpSessionAttributeListener.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class BridgeHttpSessionAttributeListener implements HttpSessionAttributeListener {

	@Override
	public void attributeAdded(HttpSessionBindingEvent event) {
		final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
		if (attributeDispatcher != null) {
			attributeDispatcher.attributeAdded(event);
		}
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) {
		final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
		if (attributeDispatcher != null) {
			attributeDispatcher.attributeRemoved(event);
		}
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent event) {
		final HttpSessionAttributeListener attributeDispatcher = getHttpSessionAttributeListener();
		if (attributeDispatcher != null) {
			attributeDispatcher.attributeReplaced(event);
		}
	}

	private HttpSessionAttributeListener getHttpSessionAttributeListener() {
		final EventDispatcherTracker tracker = EventDispatcherTrackerSupport.INSTANCE.getEventDispatcherTracker();
		if (tracker != null) {
			return tracker.getHttpSessionAttributeListener();
		}
		return null;
	}
}
