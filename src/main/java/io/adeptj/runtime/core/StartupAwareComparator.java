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

package io.adeptj.runtime.core;

import io.adeptj.runtime.common.StartupOrder;

import java.util.Comparator;

/**
 * Comparator that compares the StartupAware instances on the basis of the annotation {@link StartupOrder}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class StartupAwareComparator implements Comparator<Class<?>> {

    @Override
    public int compare(Class<?> startupAwareFirst, Class<?> startupAwareSecond) {
        StartupOrder startupOrderFirst = startupAwareFirst.getDeclaredAnnotation(StartupOrder.class);
        StartupOrder startupOrderSecond = startupAwareSecond.getDeclaredAnnotation(StartupOrder.class);
        return Integer.compare(startupOrderFirst == null ? 0 : startupOrderFirst.value(),
                startupOrderSecond == null ? 0 : startupOrderSecond.value());
    }
}
