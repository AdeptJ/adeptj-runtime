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
package com.adeptj.runtime.common;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Utility for providing time in multiple ranges.
 * 
 * Rakesh.Kumar, AdeptJ
 */
public final class TimeUnits {
	
	// No instances, just utility methods.
	private TimeUnits() {}

	/**
	 * Converts the nanoseconds time to milliseconds.
	 * 
	 * @param startTime
	 * @return time in milliseconds
	 */
	public static final long nanosToMillis(final long startTime) {
		return NANOSECONDS.toMillis(System.nanoTime() - startTime);
	}
}
