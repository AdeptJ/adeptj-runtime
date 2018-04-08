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
import io.adeptj.runtime.common.OSGiUtil;
import io.adeptj.runtime.common.Times;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends ServiceTracker<HttpServlet, HttpServlet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherServletTracker.class);

    private static final String DISPATCHER_SERVLET_FILTER = "(http.felix.dispatcher=*)";

    private volatile HttpServlet dispatcherServlet;

    DispatcherServletTracker(BundleContext context) {
        super(context, OSGiUtil.filter(context, HttpServlet.class, DISPATCHER_SERVLET_FILTER), null);
    }

    @Override
    public HttpServlet addingService(ServiceReference<HttpServlet> reference) {
        AtomicBoolean dispatcherServletInitialized = new AtomicBoolean();
        if (this.dispatcherServlet == null) {
            try {
                long startTime = System.nanoTime();
                this.dispatcherServlet = super.addingService(reference);
                LOGGER.info("Adding OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
                this.dispatcherServlet.init(BridgeServletConfigHolder.INSTANCE.getBridgeServletConfig());
                dispatcherServletInitialized.set(true);
                LOGGER.info("Felix DispatcherServlet initialized in [{}] ms!!", Times.elapsedMillis(startTime));
            } catch (ServletException ex) {
                LOGGER.error("Exception adding Felix DispatcherServlet OSGi Service!!", ex);
            }
        }
        return dispatcherServletInitialized.get() ? this.dispatcherServlet : null;
    }

    @Override
    public void removedService(ServiceReference<HttpServlet> reference, HttpServlet service) {
        LOGGER.info("Removing OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
        Optional.ofNullable(this.dispatcherServlet).ifPresent(httpServlet -> {
            try {
                httpServlet.destroy();
                LOGGER.info("Felix DispatcherServlet Destroyed!!");
            } catch (Exception ex) { // NOSONAR
                LOGGER.error(ex.getMessage(), ex);
            }
        });
        super.removedService(reference, service);
    }

    HttpServlet getDispatcherServlet() {
        return this.dispatcherServlet;
    }
}