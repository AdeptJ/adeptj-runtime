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

package com.adeptj.runtime.osgi;

import com.adeptj.runtime.common.ServletContextHolder;
import com.adeptj.runtime.common.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * ContextListener that handles the OSGi Framework shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebListener("Stops the OSGi Framework when ServletContext is destroyed")
public class FrameworkShutdownHandler implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Nothing to do here as OSGi Framework is initialized in FrameworkStartupHandler.
        // Can't do the Framework initialization here because we register EventListener(s) and HttpServlet(s)
        // using the ServletContext passed which results in java.lang.UnsupportedOperationException
        // UT010042: This method cannot be called from a ServletContextListener that has been added programmatically.
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        long startTime = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(FrameworkShutdownHandler.class);
        logger.info("Stopping OSGi Framework as ServletContext is being destroyed!!");
        logger.info("Closing EventDispatcherTracker!!");
        ServiceTrackers.INSTANCE.closeEventDispatcherTracker();
        // see - https://github.com/AdeptJ/adeptj-runtime/issues/4
        // Close the DispatcherServletTracker here rather than in BridgeServlet#destroy method.
        // As with version 3.0.18 of Felix Http base the way with HttpSessionListener(s) handled
        // is changed which results in a NPE.
        logger.info("Closing DispatcherServletTracker!!");
        ServiceTrackers.INSTANCE.closeDispatcherServletTracker();
        FrameworkManager.INSTANCE.stopFramework();
        ServletContextHolder.INSTANCE.setServletContext(null);
        logger.info("OSGi Framework stopped in [{}] ms!!", Times.elapsedMillis(startTime));
    }

}
