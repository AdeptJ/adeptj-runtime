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
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServlet;

/**
 * Support for DispatcherServletTracker.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum DispatcherServletTrackers {

    INSTANCE;

    private volatile boolean dispatcherServletInitialized;

    private DispatcherServletTracker servletTracker;

    public void openDispatcherServletTracker() {
        if (!this.dispatcherServletInitialized && this.servletTracker == null) {
            BundleContext bundleContext = BundleContextHolder.INSTANCE.getBundleContext();
            DispatcherServletTracker tracker = new DispatcherServletTracker(bundleContext);
            tracker.open();
            this.servletTracker = tracker;
            this.dispatcherServletInitialized = true;
        }
    }

    public HttpServlet getDispatcherServlet() {
        return this.servletTracker == null ? null : this.servletTracker.getDispatcherServlet();
    }

    public void closeDispatcherServletTracker() {
        this.dispatcherServletInitialized = false;
        if (this.servletTracker != null && !this.servletTracker.isEmpty()) {
            this.servletTracker.close();
        }
        this.servletTracker = null;
    }
}
