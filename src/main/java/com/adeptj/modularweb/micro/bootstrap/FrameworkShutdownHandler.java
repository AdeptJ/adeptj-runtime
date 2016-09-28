/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
*/
package com.adeptj.modularweb.micro.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextListener that handles the OSGi Framework shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkShutdownHandler implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkShutdownHandler.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {
		LOGGER.info("@@@@ FrameworkShutdownHandler.contextInitialized @@@@");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LOGGER.info("Stopping OSGi Framework as ServletContext is being destroyed!!");
		long startTime = System.currentTimeMillis();
		FrameworkBootstrap.INSTANCE.stopFramework();
		ServletContextAware.INSTANCE.setServletContext(null);
		LOGGER.info("OSGi Framework stopped in [{}] ms!!", (System.currentTimeMillis() - startTime));
	}

}
