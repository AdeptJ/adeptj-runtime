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

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.Constants;
import com.adeptj.runtime.common.ServletContextHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * OSGi FrameworkListener.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkRestartHandler implements FrameworkListener {

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
        Logger logger = LoggerFactory.getLogger(FrameworkRestartHandler.class);
        switch (event.getType()) {
            case FrameworkEvent.STARTED:
                logger.info("Handling OSGi Framework Restart!!");
                // Add the new BundleContext as a ServletContext attribute, remove the stale BundleContext.
                BundleContext bundleContext = event.getBundle().getBundleContext();
                this.handleBundleContext(bundleContext);
                this.handleDispatcherServletTracker(logger);
                break;
            case FrameworkEvent.STOPPED_UPDATE:
                logger.info("Closing DispatcherServletTracker!!");
                DispatcherServletTrackers.INSTANCE.closeDispatcherServletTracker();
                logger.info("Closing EventDispatcherTracker!!");
                EventDispatcherTrackers.INSTANCE.closeEventDispatcherTracker();
                break;
            default:
                // log it and ignore.
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring the OSGi FrameworkEvent: [{}]", FrameworkEvents.asString(event.getType()));
                }
                break;
        }
    }

    private void handleBundleContext(BundleContext bundleContext) {
        ServletContext servletContext = ServletContextHolder.INSTANCE.getServletContext();
        servletContext.removeAttribute(Constants.BUNDLE_CTX_ATTR);
        servletContext.setAttribute(Constants.BUNDLE_CTX_ATTR, bundleContext);
        // Sets the new BundleContext in BundleContextHolder so that whenever the Server shuts down
        // after a restart of OSGi Framework, it should not throw [java.lang.IllegalStateException]
        // while removing this listener by calling removeFrameworkListener method when OSGi Framework stops itself.
        BundleContextHolder.INSTANCE.setBundleContext(bundleContext);
    }

    private void handleDispatcherServletTracker(Logger logger) {
        try {
            DispatcherServletTrackers.INSTANCE.closeDispatcherServletTracker();
            logger.info("Opening DispatcherServletTracker as OSGi Framework restarted!!");
            DispatcherServletTrackers.INSTANCE.openDispatcherServletTracker();
        } catch (Exception ex) { // NOSONAR
            // Note: What shall we do if DispatcherServlet initialization failed.
            // Log it as of now, we may need to stop the OSGi Framework, will decide later.
            // Have not seen this yet.
            logger.error("Exception while restarting OSGi Framework!!", ex);
        }
    }

}
