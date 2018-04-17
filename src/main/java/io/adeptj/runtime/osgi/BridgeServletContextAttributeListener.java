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

import io.adeptj.runtime.common.BundleContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import static io.adeptj.runtime.common.Constants.ATTRIBUTE_BUNDLE_CTX;

/**
 * A {@link ServletContextAttributeListener} which initializes the {@link EventDispatcherTracker} when {@link BundleContext}
 * is being set as a {@link javax.servlet.ServletContext} attribute and again closes and opens when {@link BundleContext} is
 * replaced as a {@link javax.servlet.ServletContext} attribute.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class BridgeServletContextAttributeListener implements ServletContextAttributeListener {

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        if (StringUtils.equals(event.getName(), ATTRIBUTE_BUNDLE_CTX)) {
            ServiceTrackers.getInstance().openEventDispatcherTracker((BundleContext) event.getValue());
        }
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        if (StringUtils.equals(event.getName(), ATTRIBUTE_BUNDLE_CTX)) {
            ServiceTrackers.getInstance().closeEventDispatcherTracker();
            /*
             * Now open the EventDispatcherTracker with fresh BundleContext which is already hold by
             * BundleContextHolder after being set in FrameworkLifecycleListener.
             *
             * Rationale: If we use the BundleContext contained in the passed event which is a stale
             * BundleContext in case of a framework restart event and results in a IllegalStateException.
             */
            ServiceTrackers.getInstance()
                    .openEventDispatcherTracker(BundleContextHolder.getInstance().getBundleContext());
        }
    }
}
