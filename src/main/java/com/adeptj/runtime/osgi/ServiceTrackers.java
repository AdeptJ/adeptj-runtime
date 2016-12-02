/*
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

import org.osgi.util.tracker.ServiceTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ServiceTrackers. Utility for performing operations on OSGi ServiceTracker instances.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ServiceTrackers {

    INSTANCE;

    private Map<String, ServiceTracker<?, ?>> trackers = new HashMap<>();

    public void track(Class<? extends ServiceTracker<?, ?>> klazz, ServiceTracker<?, ?> tracker) {
        this.trackers.put(klazz.getName(), tracker);
        tracker.open();
    }

    public void close(Class<? extends ServiceTracker<?, ?>> klazz) {
        Optional.ofNullable(this.trackers.remove(klazz.getName())).ifPresent(ServiceTracker::close);
    }

    public void closeAll() {
        this.trackers.forEach((klazz, tracker) -> tracker.close());
    }

    public ServiceTracker<?, ?> getTracker(Class<? extends ServiceTracker<?, ?>> klazz) {
        return this.trackers.get(klazz.getName());
    }

    public static void close(ServiceTracker<?, ?> tracker) {
        tracker.close();
    }

    public static void closeQuietly(ServiceTracker<?, ?> tracker) {
        try {
            tracker.close();
        } catch (Exception ex) {
            // Ignore, anyway Framework is managing it as the Tracked service is being removed from service registry.
        }
    }
}
