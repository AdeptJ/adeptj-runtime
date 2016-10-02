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
package com.adeptj.modularweb.micro.bootstrap.osgi;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.adeptj.modularweb.micro.bootstrap.common.StartupOrder;
import com.adeptj.modularweb.micro.bootstrap.core.StartupHandler;
import com.adeptj.modularweb.micro.bootstrap.initializer.FrameworkServletContainerInitializer;

/**
 * StartupHandler is a {@link javax.servlet.annotation.HandlesTypes} that handles the OSGi Framework startup.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@StartupOrder(0)
public class FrameworkStartupHandler implements StartupHandler {
	
    /**
     * This method will be called by the {@link FrameworkServletContainerInitializer} while application startup is in
     * progress.
     *
     * @param context
     * @throws ServletException
     */
    @Override
	public void onStartup(ServletContext context) throws ServletException {
		FrameworkBootstrap.INSTANCE.startFramework();
	}
}
