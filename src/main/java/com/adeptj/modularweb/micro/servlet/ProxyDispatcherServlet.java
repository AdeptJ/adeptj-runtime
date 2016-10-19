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
package com.adeptj.modularweb.micro.servlet;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.osgi.DispatcherServletTrackerSupport;

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
    	long startNanos = System.nanoTime();
        LOGGER.info("Initializing ProxyDispatcherServlet!!");
        try {
        	LOGGER.info("Opening DispatcherServletTracker which initializes the Felix DispatcherServlet!!");
        	DispatcherServletTrackerSupport.INSTANCE.openDispatcherServletTracker(this.getServletConfig());
		} catch (InvalidSyntaxException ise) {
			LOGGER.error("Could not register the DispatcherServletTracker!!", ise);
			throw new ServletException("Could not register the DispatcherServletTracker!!", ise);
		}
        LOGGER.info("ProxyDispatcherServlet initialized in [{}] ms", NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    /**
     * Proxy for FELIX DispatcherServlet, delegates all the calls to the underlying DispatcherServlet.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        LOGGER.debug("Handling request: {}", req.getRequestURI());
        HttpServlet dispatcherServlet = DispatcherServletTrackerSupport.INSTANCE.getDispatcherServlet();
        try {
            if (dispatcherServlet == null) {
            	LOGGER.warn("Felix DispatcherServlet is unavailable!!");
            	res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } else {
            	dispatcherServlet.service(req, res);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception while handling request!!", ex);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Close the DispatcherServletTracker.
     */
    @Override
    public void destroy() {
        LOGGER.info("Destroying ProxyDispatcherServlet!!");
        super.destroy();
        LOGGER.info("Closing DispatcherServletTracker!!");
        DispatcherServletTrackerSupport.INSTANCE.closeDispatcherServletTracker();
    }
}