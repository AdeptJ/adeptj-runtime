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
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.util.Optional;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends BridgeServiceTracker<HttpServlet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletTracker.class);

    /**
     * This is an instance of Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    private volatile HttpServlet dispatcherServlet;

    DispatcherServletTracker(BundleContext context) {
        super(context, HttpServlet.class);
    }

    @Override
    protected void setService(HttpServlet httpServlet) {
        try {
            long startTime = System.nanoTime();
            this.dispatcherServlet = httpServlet;
            this.dispatcherServlet.init(BridgeServletConfigHolder.INSTANCE.getBridgeServletConfig());
            LOGGER.info("Felix DispatcherServlet initialized in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Exception ex) { // NOSONAR
            // What if DispatcherServlet is initialized with passed service instance but init method failed.
            // DispatcherServlet has to be initialized fully to perform the dispatcher tasks.
            // Set the instance null so that http status 503 can be returned by BridgeServlet
            this.dispatcherServlet = null;
            LOGGER.error("Exception adding Felix DispatcherServlet OSGi Service!!", ex);
        }
    }

    @Override
    protected void unsetService() {
        Optional.ofNullable(this.dispatcherServlet).ifPresent(dispatcher -> {
            try {
                dispatcher.destroy();
                LOGGER.info("Felix DispatcherServlet Destroyed!!");
            } catch (Exception ex) { // NOSONAR
                LOGGER.error(ex.getMessage(), ex);
            }
        });
        this.dispatcherServlet = null;
        this.serviceRemoved.set(true);
    }

    @Override
    protected HttpServlet getTrackedService() {
        return this.dispatcherServlet;
    }
}