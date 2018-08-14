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
import org.apache.felix.http.base.internal.dispatch.DispatcherServlet;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

/**
 * OSGi ServiceTracker for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletTracker extends BridgeServiceTracker<HttpServlet> {

    private static final String EXCEPTION_MSG = "Exception adding Felix DispatcherServlet OSGi Service!!";

    private volatile DispatcherServletWrapper dispatcherServletWrapper;

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
     * @param dispatcherServlet the Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    @Override
    protected HttpServlet addingService(HttpServlet dispatcherServlet) {
        this.dispatcherServletWrapper = new DispatcherServletWrapper(dispatcherServlet);
        try {
            this.dispatcherServletWrapper.init(BridgeServletConfigHolder.getInstance().getBridgeServletConfig());
        } catch (Exception ex) { // NOSONAR
            /*
             * If the init method failed above then DispatcherServlet won't be usable at all.
             * DispatcherServlet has to be initialized fully to perform the dispatcher tasks.
             * Set the instance null so that http status 503 can be returned by BridgeServlet.
             * Also, the ServiceTracker will not be tracking it at all when this method returns null.
             */
            this.dispatcherServletWrapper = null;
            LoggerFactory.getLogger(this.getClass()).error(EXCEPTION_MSG, ex);
        }
        return this.dispatcherServletWrapper;
    }

    /**
     * Calls the Felix {@link DispatcherServlet#destroy()} and set the tracked instance to null.
     *
     * @param dispatcherServlet the Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}
     */
    @Override
    protected void removedService(HttpServlet dispatcherServlet) {
        this.dispatcherServletWrapper.destroy();
        this.dispatcherServletWrapper = null;
    }

    HttpServlet getDispatcherServlet() {
        return this.dispatcherServletWrapper;
    }
}