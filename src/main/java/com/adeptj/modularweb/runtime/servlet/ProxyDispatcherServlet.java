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
package com.adeptj.modularweb.runtime.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.common.TimeUnits;
import com.adeptj.modularweb.runtime.osgi.DispatcherServletTrackerSupport;

/**
 * HttpServlet acting as a front controller for all of the incoming requests and
 * delegates the actual service request to FELIX DispatcherServlet, so in other
 * words it is acting as a Proxy for FELIX DispatcherServlet.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ProxyDispatcherServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDispatcherServlet.class);

    private static final long serialVersionUID = 702778293237417284L;

    /**
     * Open the DispatcherServletTracker.
     */
    @Override
    public void init() throws ServletException {
    	long startTime = System.nanoTime();
        LOGGER.info("Initializing ProxyDispatcherServlet!!");
        try {
        	LOGGER.info("Opening DispatcherServletTracker which initializes the Felix DispatcherServlet!!");
        	DispatcherServletTrackerSupport.INSTANCE.openDispatcherServletTracker(this.getServletConfig());
		} catch (InvalidSyntaxException ise) {
			LOGGER.error("Could not register the DispatcherServletTracker!!", ise);
			throw new ServletException("Could not register the DispatcherServletTracker!!", ise);
		}
        LOGGER.info("ProxyDispatcherServlet initialized in [{}] ms", TimeUnits.nanosToMillis(startTime));
    }

    /**
     * Proxy for FELIX DispatcherServlet, delegates all the calls to the underlying DispatcherServlet.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.debug("Handling request: {}", req.getRequestURI());
        HttpServlet dispatcherServlet = DispatcherServletTrackerSupport.INSTANCE.getDispatcherServlet();
        try {
            if (dispatcherServlet == null) {
            	LOGGER.warn("Can't serve request as Felix DispatcherServlet is unavailable!!");
            	resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
            	dispatcherServlet.service(req, resp);
    			this.logDispatcherException(req);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception while handling request!!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

	private void logDispatcherException(HttpServletRequest req) {
		// Check if [javax.servlet.error.exception] set by [org.apache.felix.http.base.internal.dispatch.Dispatcher]
		Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		if (exception != null) {
			LOGGER.error("Exception while handling request!!", exception);
		}
	}

    /**
     * Close the DispatcherServletTracker.
     */
    @Override
    public void destroy() {
        LOGGER.info("Destroying ProxyDispatcherServlet which in turn closes DispatcherServletTracker!!");
        DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
    }
}