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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.osgi.DispatcherServletTrackerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ProxyServlet acts as a facade for all of the incoming requests for OSGi resources.
 * Delegates the service request to Felix DispatcherServlet which maintains a registry of HttpServlet/Filter etc.
 * Depending upon the resolution by DispatcherServlet the request is being further dispatched.
 * 
 * <p>
 * <b>This HttpServlet listens at "/" i.e root.<b>
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ProxyServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServlet.class);

    private static final long serialVersionUID = 702778293237417284L;

    /**
     * Open the DispatcherServletTracker.
     */
    @Override
    public void init() throws ServletException {
        long startTime = System.nanoTime();
        LOGGER.info("Initializing ProxyServlet!!");
        LOGGER.info("Opening DispatcherServletTracker which initializes the Felix DispatcherServlet!!");
        DispatcherServletTrackerSupport.INSTANCE.openDispatcherServletTracker(this.getServletConfig());
        LOGGER.info("ProxyServlet initialized in [{}] ms!!", Times.elapsedSinceMillis(startTime));
    }

    /**
     * Proxy for Felix DispatcherServlet, delegate all the calls to the underlying DispatcherServlet.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpServlet dispatcherServlet = DispatcherServletTrackerSupport.INSTANCE.getDispatcherServlet();
        try {
            if (dispatcherServlet == null) {
                LOGGER.error("Can't serve request: [{}], DispatcherServlet is unavailable!!", req.getRequestURI());
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
            	long startTime = System.nanoTime();
            	dispatcherServlet.service(req, resp);
                LOGGER.debug("Request: [{}] took [{}] ms!!", req.getRequestURI(), Times.elapsedSinceMillis(startTime));
                this.checkAndlogExceptionAttribute(req);
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while handling request!!", ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

	private void checkAndlogExceptionAttribute(HttpServletRequest req) {
		// Check if [javax.servlet.error.exception] set by [org.apache.felix.http.base.internal.dispatch.Dispatcher]
		if (req.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null) {
		    LOGGER.error("Exception while handling request!!", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
		}
	}

    /**
     * Close the DispatcherServletTracker.
     */
    @Override
    public void destroy() {
    	LOGGER.info("Destroying ProxyServlet!!");
    	// closeDispatcherServletTracker in FrameworkShutdownHandler, see - https://github.com/AdeptJ/adeptj-runtime/issues/4
    }
}