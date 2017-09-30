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

package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.Requests;
import com.adeptj.runtime.common.ServletConfigs;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.osgi.ServiceTrackers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * BridgeServlet acts as a bridge between ServletContainer and embedded OSGi HttpService.
 * <p>
 * All the incoming requests for OSGi resources delegated to the Felix DispatcherServlet which further
 * delegates to Felix {@link org.apache.felix.http.base.internal.dispatch.Dispatcher} which maintains
 * a registry of managed HttpServlet/Filter(s) etc.
 * <p>
 * Depending upon the resolution by Felix Dispatcher the request is being further dispatched to actual HttpServlet/Filter.
 * <p>
 * <p>
 * <b>This HttpServlet listens at "/" i.e root.<b>
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class BridgeServlet extends HttpServlet {

    private static final long serialVersionUID = 702778293237417284L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeServlet.class);

    private static final String UNAVAILABLE_MSG = "Can't serve request: [{}], DispatcherServlet is unavailable!!";

    /**
     * Open the DispatcherServletTracker.
     */
    @Override
    public void init() throws ServletException {
        long startTime = System.nanoTime();
        LOGGER.info("Initializing BridgeServlet!!");
        LOGGER.info("Opening DispatcherServletTracker which initializes the Felix DispatcherServlet!!");
        // Store BridgeServlet's ServletConfig which is used to init Felix DispatcherServlet.
        ServletConfigs.INSTANCE.add(BridgeServlet.class, this.getServletConfig());
        ServiceTrackers.INSTANCE.openDispatcherServletTracker();
        LOGGER.info("BridgeServlet initialized in [{}] ms!!", Times.elapsedMillis(startTime));
    }

    /**
     * Bridge for Felix DispatcherServlet, delegate all the calls to underlying DispatcherServlet.
     *
     * @see class header for detailed description.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpServlet dispatcherServlet = ServiceTrackers.INSTANCE.getDispatcherServlet();
        try {
            if (dispatcherServlet == null) {
                LOGGER.error(UNAVAILABLE_MSG, req.getRequestURI());
                resp.sendError(SC_SERVICE_UNAVAILABLE);
            } else {
                dispatcherServlet.service(req, resp);
                // Check if [javax.servlet.error.exception] set by [Felix Dispatcher]
                if (Requests.hasException(req)) {
                    LOGGER.error("Exception set by Felix Dispatcher!!", Requests.attr(req, ERROR_EXCEPTION));
                }
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while handling request: [{}]", req.getRequestURI(), ex);
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Close the DispatcherServletTracker.
     */
    @Override
    public void destroy() {
        LOGGER.info("Destroying BridgeServlet!!");
        // closeDispatcherServletTracker in FrameworkShutdownHandler
        // See - https://github.com/AdeptJ/adeptj-runtime/issues/4
    }
}