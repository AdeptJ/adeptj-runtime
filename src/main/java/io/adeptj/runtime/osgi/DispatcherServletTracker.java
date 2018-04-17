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

package io.adeptj.runtime.osgi;

import io.adeptj.runtime.common.BridgeServletConfigHolder;
import io.adeptj.runtime.common.Times;
import org.apache.felix.http.base.internal.dispatch.DispatcherServlet;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends BridgeServiceTracker<HttpServlet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletTracker.class);

    /**
     * The Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    private volatile HttpServlet dispatcherServlet;

    /**
     * Create the {@link org.osgi.util.tracker.ServiceTracker} for {@link HttpServlet}
     *
     * @param context the {@link BundleContext}
     */
    DispatcherServletTracker(BundleContext context) {
        super(context, HttpServlet.class);
    }

    /**
     * Initializes the Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     *
     * @param service the Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    @Override
    protected HttpServlet setup(HttpServlet service) {
        long startTime = System.nanoTime();
        this.dispatcherServlet = service;
        try {
            this.dispatcherServlet.init(BridgeServletConfigHolder.getInstance().getBridgeServletConfig());
            LOGGER.info("Felix DispatcherServlet initialized in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Exception ex) { // NOSONAR
            /*
             * If the init method failed above then DispatcherServlet won't be usable at all.
             * DispatcherServlet has to be initialized fully to perform the dispatcher tasks.
             * Set the instance null so that http status 503 can be returned by BridgeServlet.
             * Also the ServiceTracker will not be tracking it at all when addingService method returns.
             */
            this.dispatcherServlet = null;
            LOGGER.error("Exception adding Felix DispatcherServlet OSGi Service!!", ex);
        }
        return this.dispatcherServlet;
    }

    /**
     * Calls the Felix {@link DispatcherServlet#destroy()} and set the tracked instance to null.
     */
    @Override
    protected void cleanup() {
        this.dispatcherServlet.destroy();
        LOGGER.info("Felix DispatcherServlet Destroyed!!");
        this.dispatcherServlet = null;
    }

    HttpServlet getDispatcherServlet() {
        return this.dispatcherServlet;
    }
}