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

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import static io.adeptj.runtime.common.Constants.BUNDLE_CTX_ATTR;

/**
 * BridgeServletContextAttributeListener.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class BridgeServletContextAttributeListener implements ServletContextAttributeListener {

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        if (this.isAttributeBundleContext(event.getName())) {
            ServiceTrackers.INSTANCE.openEventDispatcherTracker((BundleContext) event.getValue());
        }
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        if (this.isAttributeBundleContext(event.getName())) {
            ServiceTrackers.INSTANCE.closeEventDispatcherTracker();
        }
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        // Does nothing as of now.
    }

    private boolean isAttributeBundleContext(String attributeName) {
        return BUNDLE_CTX_ATTR.equals(attributeName);
    }
}
