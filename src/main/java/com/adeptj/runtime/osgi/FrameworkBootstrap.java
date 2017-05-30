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
package com.adeptj.runtime.osgi;

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.servlet.OSGiPerServletContextErrorServlet;
import com.adeptj.runtime.servlet.ProxyServlet;
import com.adeptj.runtime.servlet.RESTEasyServlet;
import com.typesafe.config.Config;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * FrameworkBootstrap that handles the OSGi Framework(Apache Felix) startup and shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkBootstrap {

    INSTANCE;

    private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

    private static final String PROXY_SERVLET = "ProxyServlet";

    private static final String ROOT_MAPPING = "/*";

    private Framework framework;

    private FrameworkRestartHandler frameworkListener;

    public void startFramework(ServletContext context) {
        Logger logger = LoggerFactory.getLogger(FrameworkBootstrap.class);
        try {
            logger.info("Starting the OSGi Framework!!");
            long startTime = System.nanoTime();
            this.framework = this.createFramework(logger);
            this.framework.start();
            this.frameworkListener = new FrameworkRestartHandler();
            BundleContext systemBundleContext = this.framework.getBundleContext();
            systemBundleContext.addFrameworkListener(this.frameworkListener);
            BundleContextHolder.INSTANCE.setBundleContext(systemBundleContext);
            Bundles.provisionBundles(systemBundleContext);
            List<String> errorPages = Configs.DEFAULT.undertow().getStringList("common.error-pages");
            OSGiServlets.INSTANCE.registerErrorServlet(systemBundleContext, new OSGiPerServletContextErrorServlet(), errorPages);
            OSGiServlets.INSTANCE.register(systemBundleContext, new RESTEasyServlet());
            logger.info("OSGi Framework started in [{}] ms!!", Times.elapsedSinceMillis(startTime));
            this.registerBridgeListeners(context);
            // Set the BundleContext as a ServletContext attribute as per Felix HttpBridge Specification.
            context.setAttribute(BundleContext.class.getName(), systemBundleContext);
            this.registerProxyServlet(context, logger);
        } catch (Exception ex) { // NOSONAR
            logger.error("Failed to start OSGi Framework!!", ex);
            // Stop the Framework if the Bundles throws exception.
            this.stopFramework();
        }
    }

    public void stopFramework() {
        Logger logger = LoggerFactory.getLogger(FrameworkBootstrap.class);
        try {
            if (this.framework != null) {
                this.removeFrameworkListener();
                OSGiServlets.INSTANCE.unregisterAll();
                this.framework.stop();
                // A value of zero will wait indefinitely.
                FrameworkEvent event = this.framework.waitForStop(0);
                logger.info("OSGi FrameworkEvent: [{}]", FrameworkEvents.asString(event.getType())); // NOSONAR
            } else {
                logger.info("OSGi Framework not started yet, nothing to stop!!");
            }
        } catch (Exception ex) { // NOSONAR
            logger.error("Error Stopping OSGi Framework!!", ex);
        }
    }

    private void removeFrameworkListener() {
        if (BundleContextHolder.INSTANCE.isBundleContextValid()) {
            BundleContextHolder.INSTANCE.getBundleContext().removeFrameworkListener(this.frameworkListener);
        }
    }

    private void registerBridgeListeners(ServletContext servletContext) {
        // add all required listeners
        servletContext.addListener(new BridgeServletContextAttributeListener());
        servletContext.addListener(new BridgeHttpSessionListener());
        servletContext.addListener(new BridgeHttpSessionIdListener());
        servletContext.addListener(new BridgeHttpSessionAttributeListener());
    }

    private void registerProxyServlet(ServletContext context, Logger logger) {
        // Register the ProxyServlet after the OSGi Framework started successfully.
        // This will ensure that the Felix {@link DispatcherServlet} is available as an OSGi service and can be tracked.
        // ProxyServlet delegates all the service calls to the Felix DispatcherServlet.
        ServletRegistration.Dynamic proxyServlet = context.addServlet(PROXY_SERVLET, new ProxyServlet());
        proxyServlet.addMapping(ROOT_MAPPING);
        // Required if [osgi.http.whiteboard.servlet.asyncSupported] is declared true for OSGi HttpService managed Servlets.
        // Otherwise the request processing fails throwing exception [java.lang.IllegalStateException: UT010026:
        // Async is not supported for this request, as not all filters or Servlets were marked as supporting async]
        proxyServlet.setAsyncSupported(true);
        // Load early to detect any issue with OSGi Felix DispatcherServlet initialization.
        proxyServlet.setLoadOnStartup(0);
        logger.info("ProxyServlet registered successfully!!");
    }

    private Framework createFramework(Logger logger) throws IOException  {
        return ServiceLoader.load(FrameworkFactory.class).iterator().next().newFramework(this.frameworkConfigs(logger));
    }

    private Map<String, String> frameworkConfigs(Logger logger) throws IOException {
        Map<String, String> configs = this.loadFrameworkProperties();
        Config felixConf = Configs.DEFAULT.felix();
        configs.put("felix.cm.dir", felixConf.getString("felix-cm-dir"));
        configs.put("felix.memoryusage.dump.location", felixConf.getString("memoryusage-dump-loc"));
        logger.debug("OSGi Framework Configurations: {}", configs);
        return configs;
    }

    private Map<String, String> loadFrameworkProperties() throws IOException {
        Properties props = new Properties();
        props.load(FrameworkBootstrap.class.getResourceAsStream(FRAMEWORK_PROPERTIES));
        Map<String, String> configs = new HashMap<>();
        props.forEach((key, val) -> configs.put((String) key, (String) val));
        return configs;
    }
}