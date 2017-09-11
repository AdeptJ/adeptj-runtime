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

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.servlet.BridgeServlet;
import com.adeptj.runtime.servlet.osgi.PerServletContextErrorServlet;
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
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static java.lang.System.getProperty;

/**
 * FrameworkBootstrap that handles the OSGi Framework(Apache Felix) startup and shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkBootstrap {

    INSTANCE;

    private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

    private static final String BRIDGE_SERVLET = "AdeptJ BridgeServlet";

    private static final String ROOT_MAPPING = "/*";

    private static final String FELIX_CM_DIR = "felix.cm.dir";

    private static final String CFG_KEY_FELIX_CM_DIR = "felix-cm-dir";

    private static final String MEM_DUMP_LOC = "felix.memoryusage.dump.location";

    private static final String CFG_KEY_MEM_DUMP_LOC = "memoryusage-dump-loc";

    private static final String FELIX_LOG_LEVEL = "felix.log.level";

    private Framework framework;

    private FrameworkRestartHandler frameworkListener;

    public void startFramework(ServletContext context) {
        Logger logger = LoggerFactory.getLogger(FrameworkBootstrap.class);
        try {
            logger.info("Starting the OSGi Framework!!");
            long startTime = System.nanoTime();
            // config directory will not yet be created if framework is being provisioned first time.
            boolean configDirExists = Paths.get(Configs.DEFAULT.felix().getString(CFG_KEY_FELIX_CM_DIR))
                    .toFile()
                    .exists();
            this.framework = this.createFramework(logger);
            long startTimeFramework = System.nanoTime();
            this.framework.start();
            logger.info("Framework creation took [{}] ms!!", Times.elapsedSinceMillis(startTimeFramework));
            BundleContext systemBundleContext = this.framework.getBundleContext();
            this.frameworkListener = new FrameworkRestartHandler();
            systemBundleContext.addFrameworkListener(this.frameworkListener);
            BundleContextHolder.INSTANCE.setBundleContext(systemBundleContext);
            if (configDirExists && !Boolean.getBoolean("provision.bundles.explicitly")) {
                logger.info("Bundles already provisioned, this must be a server restart!!");
            } else {
                logger.info("Provisioning bundles first time!!");
                Bundles.provisionBundles(systemBundleContext);
            }
            OSGiServlets.INSTANCE.registerErrorServlet(systemBundleContext, new PerServletContextErrorServlet(),
                    Configs.DEFAULT.undertow().getStringList("common.osgi-error-pages"));
            logger.info("OSGi Framework started in [{}] ms!!", Times.elapsedSinceMillis(startTime));
            this.registerBridgeListeners(context);
            // Set the BundleContext as a ServletContext attribute as per Felix HttpBridge Specification.
            context.setAttribute(BundleContext.class.getName(), systemBundleContext);
            this.registerBridgeServlet(context, logger);
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
        if (BundleContextHolder.INSTANCE.isBundleContextAvailable()) {
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

    private void registerBridgeServlet(ServletContext context, Logger logger) {
        // Register the BridgeServlet after the OSGi Framework started successfully.
        // This will ensure that the Felix DispatcherServlet is available as an OSGi service and can be tracked.
        // BridgeServlet delegates all the service calls to the Felix DispatcherServlet.
        ServletRegistration.Dynamic bridgeServlet = context.addServlet(BRIDGE_SERVLET, new BridgeServlet());
        bridgeServlet.addMapping(ROOT_MAPPING);
        // Required if [osgi.http.whiteboard.servlet.asyncSupported] is declared true for OSGi HttpService managed Servlets.
        // Otherwise the request processing fails throwing exception [java.lang.IllegalStateException: UT010026:
        // Async is not supported for this request, as not all filters or Servlets were marked as supporting async]
        bridgeServlet.setAsyncSupported(true);
        // Load early to detect any issue with OSGi Felix DispatcherServlet initialization.
        bridgeServlet.setLoadOnStartup(0);
        logger.info("BridgeServlet registered successfully!!");
    }

    private Framework createFramework(Logger logger) throws IOException {
        return ServiceLoader.load(FrameworkFactory.class)
                .iterator()
                .next()
                .newFramework(this.frameworkConfigs(logger));
    }

    private Map<String, String> frameworkConfigs(Logger logger) throws IOException {
        Map<String, String> configs = this.loadFrameworkProperties();
        Config felixConf = Configs.DEFAULT.felix();
        configs.put(FELIX_CM_DIR, felixConf.getString(CFG_KEY_FELIX_CM_DIR));
        configs.put(MEM_DUMP_LOC, felixConf.getString(CFG_KEY_MEM_DUMP_LOC));
        Optional.ofNullable(getProperty(FELIX_LOG_LEVEL)).ifPresent(level -> configs.put(FELIX_LOG_LEVEL, level));
        if (logger.isDebugEnabled()) {
            logger.debug("OSGi Framework Configurations: {}", configs);
        }
        return configs;
    }

    private Map<String, String> loadFrameworkProperties() throws IOException {
        Properties props = new Properties();
        Map<String, String> configs = new HashMap<>();
        try (InputStream is = FrameworkBootstrap.class.getResourceAsStream(FRAMEWORK_PROPERTIES)) {
            props.load(is);
            props.forEach((key, val) -> configs.put((String) key, (String) val));
        }
        return configs;
    }
}