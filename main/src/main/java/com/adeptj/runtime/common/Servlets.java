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

package com.adeptj.runtime.common;

import com.adeptj.runtime.osgi.BridgeHttpSessionIdListener;
import com.adeptj.runtime.osgi.BridgeHttpSessionListener;
import com.adeptj.runtime.osgi.BridgeServletContextAttributeListener;
import com.adeptj.runtime.servlet.BridgeServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.adeptj.runtime.servlet.BridgeServlet.BRIDGE_SERVLET_NAME;
import static com.adeptj.runtime.servlet.BridgeServlet.BRIDGE_SERVLET_MOUNT_PATH;

/**
 * Utility for servlet, filters and listeners
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class Servlets {

    private static final Logger LOGGER = LoggerFactory.getLogger(Servlets.class);

    private Servlets() {
    }

    public static void registerBridgeListeners(ServletContext servletContext) {
        servletContext.addListener(new BridgeServletContextAttributeListener());
        servletContext.addListener(new BridgeHttpSessionListener());
        servletContext.addListener(new BridgeHttpSessionIdListener());
    }

    public static void registerBridgeServlet(ServletContext context) {
        // Register the BridgeServlet after the OSGi Framework started successfully.
        // This will ensure that the Felix DispatcherServlet is available as an OSGi service and can be tracked.
        // BridgeServlet delegates all the service calls to the Felix DispatcherServlet.
        ServletRegistration.Dynamic bridgeServlet = context.addServlet(BRIDGE_SERVLET_NAME, new BridgeServlet());
        Set<String> existingMappings = bridgeServlet.addMapping(BRIDGE_SERVLET_MOUNT_PATH);
        if (!existingMappings.isEmpty()) {
            LOGGER.error("Servlet mapping [/*] is already taken, this may be a misconfiguration because " +
                    " BridgeServlet must be mounted at [/*]!");
        }
        // Required if [osgi.http.whiteboard.servlet.asyncSupported] is declared true for OSGi HttpService managed Servlets.
        // Otherwise, the request processing fails throwing exception.
        // [java.lang.IllegalStateException: UT010026: Async is not supported for this request, as not all filters or Servlets
        // were marked as supporting async]
        bridgeServlet.setAsyncSupported(true);
        // Load early to detect any issue with OSGi Felix DispatcherServlet initialization.
        bridgeServlet.setLoadOnStartup(0);
    }
}
