/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, Rakesh Kumar, AdeptJ (http://adeptj.com)                 #
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
package com.adeptj.modularweb.micro.osgi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;
import com.adeptj.modularweb.micro.common.TimeUnits;

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
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		long startTime = System.nanoTime();
		Logger logger = LoggerFactory.getLogger(FrameworkShutdownHandler.class);
		logger.info("Stopping OSGi Framework as ServletContext is being destroyed!!");
		logger.info("Closing EventDispatcherTracker!!");
		EventDispatcherTrackerSupport.INSTANCE.closeEventDispatcherTracker();
		FrameworkProvisioner.INSTANCE.stopFramework();
		ServletContextAware.INSTANCE.setServletContext(null);
		logger.info("OSGi Framework stopped in [{}] ms!!", TimeUnits.nanosToMillis(startTime));
	}

}
