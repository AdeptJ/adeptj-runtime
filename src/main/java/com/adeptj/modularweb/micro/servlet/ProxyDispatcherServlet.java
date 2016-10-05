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
package com.adeptj.modularweb.micro.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;
import com.adeptj.modularweb.micro.osgi.DispatcherServletTracker;

/**
 * HttpServlet acting as a front controller for all of the incoming requests and
 * delegates the actual service request to Felix DispatcherServlet, so in other
 * words it is acting as a proxy for Felix DispatcherServlet.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ProxyDispatcherServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDispatcherServlet.class);

    private static final long serialVersionUID = 702778293237417284L;

    private DispatcherServletTracker tracker;

    private static volatile boolean initialized;

    /**
     * Open the DispatcherServletTracker.
     */
    @Override
    public void init() throws ServletException {
    	long startTime = System.currentTimeMillis();
        LOGGER.info("Initializing ProxyDispatcherServlet!!");
        if (!initialized) {
            try {
				LOGGER.info("Opening Felix DispatcherServlet ServiceTracker!!");
				this.tracker = new DispatcherServletTracker(ServletContextAware.INSTANCE.getBundleContext(),
						this.getServletConfig());
				this.tracker.open();
				initialized = true;
				LOGGER.info("ProxyDispatcherServlet initialized in [{}] ms", (System.currentTimeMillis() - startTime));
			} catch (InvalidSyntaxException ise) {
                LOGGER.error("Could not register the DispatcherServletTracker!!", ise);
                throw new ServletException("Could not register the DispatcherServletTracker!!", ise);
            }
        }
    }

    /**
     * Proxy for Felix DispatcherServlet, delegates all the calls to the underlying DispatcherServlet.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        LOGGER.debug("Handling request: {}", req.getRequestURI());
        HttpServlet dispatcher = this.tracker.getDispatcher();
        try {
            if (dispatcher == null) {
            	LOGGER.warn("DispatcherServlet not ready yet!!");
            	res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            	return;
            } else {
            	dispatcher.service(req, res);
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
        this.tracker.close();
        initialized = false;
        super.destroy();
    }

    public void stopTracker() {
        initialized = false;
        this.tracker.close();
    }

    public boolean isInitialized() {
        return initialized;
    }

}