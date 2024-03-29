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

package com.adeptj.runtime.osgi;

import com.adeptj.runtime.kernel.util.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.Serial;

/**
 * {@link HttpServlet} wrapper for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletWrapper extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 3090993776785941111L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletWrapper.class);

    /**
     * The Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    private final HttpServlet dispatcherServlet;

    DispatcherServletWrapper(HttpServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        long startTime = System.nanoTime();
        super.init(config);
        this.dispatcherServlet.init(config);
        LOGGER.info("Felix DispatcherServlet initialized in [{}] ms!!", Times.elapsedMillis(startTime));
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        this.dispatcherServlet.service(req, res);
    }

    @Override
    public void destroy() {
        this.dispatcherServlet.destroy();
        LOGGER.info("Felix DispatcherServlet Destroyed!!");
    }
}
