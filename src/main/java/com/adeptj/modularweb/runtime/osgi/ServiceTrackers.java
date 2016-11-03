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
package com.adeptj.modularweb.runtime.osgi;

import org.osgi.util.tracker.ServiceTracker;

/**
 * ServiceTrackers. Utility for performing operations on OSGi ServiceTracker instances.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class ServiceTrackers {

	// Only static utility methods.
	private ServiceTrackers() {}
	
	public static void close(ServiceTracker<?, ?> tracker) {
		tracker.close();
	}
	
	public static void closeQuietly(ServiceTracker<?, ?> tracker) {
		try {
			tracker.close();
		} catch (Exception ex) {
			// ignore, anyway Framework is managing it as the EventDispatcher is being removed from service registry.
		}
	}
}
