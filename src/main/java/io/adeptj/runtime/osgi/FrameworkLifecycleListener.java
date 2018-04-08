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
import io.adeptj.runtime.common.ServletContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

import static io.adeptj.runtime.common.Constants.BUNDLE_CTX_ATTR;
import static org.osgi.framework.FrameworkEvent.STARTED;
import static org.osgi.framework.FrameworkEvent.STOPPED_UPDATE;

/**
 * OSGi FrameworkListener which takes care of OSGi framework restart.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkLifecycleListener implements FrameworkListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkLifecycleListener.class);

    /**
     * Handles OSGi Framework restart, does following on System Bundle STARTED event.
     * <p>
     * 1. Removes the BundleContext from ServletContext attributes. <br>
     * 2. Gets the new BundleContext from System Bundle and sets that to ServletContext. <br>
     * 3. Closes the DispatcherServletTracker and open again with new BundleContext. <br>
     * 4. As the BundleContext is removed and added from ServletContext the corresponding BridgeServletContextAttributeListener
     * is fired with events which in turn closes the EventDispatcherTracker and open again with new BundleContext.
     */
    @Override
    public void frameworkEvent(FrameworkEvent event) {
        switch (event.getType()) {
            case STARTED:
                LOGGER.info("Handling OSGi Framework Restart!!");
                // Add the new BundleContext as a ServletContext attribute, remove the stale BundleContext.
                BundleContext bundleContext = event.getBundle().getBundleContext();
                BundleContextHolder.INSTANCE.setBundleContext(bundleContext);
                ServletContext servletContext = ServletContextHolder.INSTANCE.getServletContext();
                servletContext.setAttribute(BUNDLE_CTX_ATTR, bundleContext);
                ServiceTrackers.INSTANCE.closeDispatcherServletTracker();
                LOGGER.info("Opening DispatcherServletTracker as OSGi Framework restarted!!");
                ServiceTrackers.INSTANCE.openDispatcherServletTracker();
                break;
            case STOPPED_UPDATE:
                LOGGER.info("Closing DispatcherServletTracker!!");
                ServiceTrackers.INSTANCE.closeDispatcherServletTracker();
                LOGGER.info("Closing EventDispatcherTracker!!");
                ServiceTrackers.INSTANCE.closeEventDispatcherTracker();
                break;
            default:
                // log it and ignore as we are not interested in any other event.
                String eventType = FrameworkEvents.asString(event.getType());
                if (!StringUtils.equals(FrameworkEvents.UNKNOWN.toString(), eventType)) {
                    LOGGER.debug("Ignoring the OSGi FrameworkEvent: [{}]", eventType);
                }
                break;
        }
    }

}
