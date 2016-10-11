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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import com.adeptj.modularweb.micro.common.BundleContextAware;

/**
 * Support for DispatcherServletTracker.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum DispatcherServletTrackerSupport {

	INSTANCE;

	private volatile boolean isDispatcherServletInitialized;

	private volatile DispatcherServletTracker dispatcherServletTracker;

	private volatile ServletConfig servletConfig;

	public void openDispatcherServletTracker(ServletConfig servletConfig) throws InvalidSyntaxException {
		this.initServletConfig(servletConfig);
		if (!this.isDispatcherServletInitialized && this.dispatcherServletTracker == null) {
			BundleContext ctx = BundleContextAware.INSTANCE.getBundleContext();
			DispatcherServletTracker dispatcherServletTracker = new DispatcherServletTracker(ctx);
			dispatcherServletTracker.open();
			this.dispatcherServletTracker = dispatcherServletTracker;
			this.isDispatcherServletInitialized = true;
		}
	}

	public HttpServlet getDispatcherServlet() {
		return this.dispatcherServletTracker.getDispatcherServlet();
	}

	public void closeDispatcherServletTracker() {
		this.isDispatcherServletInitialized = false;
		if (this.dispatcherServletTracker != null && !this.dispatcherServletTracker.isEmpty()) {
			this.dispatcherServletTracker.close();
			this.dispatcherServletTracker = null;
		}
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	private void initServletConfig(ServletConfig servletConfig) {
		if (this.servletConfig == null) {
			this.servletConfig = servletConfig;
		}
	}
}
