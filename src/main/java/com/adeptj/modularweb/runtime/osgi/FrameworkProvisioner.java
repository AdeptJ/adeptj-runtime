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
package com.adeptj.modularweb.runtime.osgi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.common.BundleContextAware;
import com.adeptj.modularweb.runtime.common.TimeUnits;
import com.adeptj.modularweb.runtime.config.Configs;
import com.adeptj.modularweb.runtime.servlet.ProxyDispatcherServlet;
import com.typesafe.config.Config;

/**
 * FrameworkProvisioner that handles the OSGi Framework(Apache FELIX) startup and shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkProvisioner {

    INSTANCE;

    private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

    private static final String PROXY_DISPATCHER_SERVLET = "ProxyDispatcherServlet";

    private static final String ROOT_MAPPING = "/*";

    private Framework framework;

    private FrameworkRestartHandler frameworkListener;
    
    public void startFramework(ServletContext context) {
    	Logger logger = LoggerFactory.getLogger(FrameworkProvisioner.class);
        try {
        	logger.info("Starting the OSGi Framework!!");
    		long startTime = System.nanoTime();
            this.framework = this.createFramework(logger);
            this.framework.start();
            this.frameworkListener = new FrameworkRestartHandler();
            BundleContext systemBundleContext = this.framework.getBundleContext();
            systemBundleContext.addFrameworkListener(this.frameworkListener);
            BundleContextAware.INSTANCE.setBundleContext(systemBundleContext);
            BundleProvisioner.provisionBundles(systemBundleContext);
            logger.info("OSGi Framework started in [{}] ms!!", TimeUnits.nanosToMillis(startTime));
            this.initBridgeListeners(context);
            // Set the BundleContext as a ServletContext attribute as per FELIX HttpBridge Specification.
            context.setAttribute(BundleContext.class.getName(), systemBundleContext);
            this.registerProxyDispatcherServlet(context, logger);
        } catch (Exception ex) {
            logger.error("Failed to start OSGi Framework!!", ex);
            // Stop the Framework if the BundleProvisioner throws exception.
            this.stopFramework();
        }
    }

    public void stopFramework() {
    	Logger logger = LoggerFactory.getLogger(FrameworkProvisioner.class);
        try {
        	if (this.framework != null) {
        		this.removeFrameworkListener();
                this.framework.stop();
                // A value of zero will wait indefinitely.
                FrameworkEvent event = this.framework.waitForStop(0);
                logger.info("OSGi Framework Stopped, Event Code: [{}]", event.getType());
        	} else {
        		logger.info("OSGi Framework not started yet, nothing to stop!!");
        	}
        } catch (Exception ex) {
        	logger.error("Error Stopping OSGi Framework!!", ex);
        }
    }

	private void removeFrameworkListener() {
		if (BundleContextAware.INSTANCE.isBundleContextSet()) {
			BundleContextAware.INSTANCE.getBundleContext().removeFrameworkListener(this.frameworkListener);
		}
	}
    
	private void initBridgeListeners(ServletContext servletContext) {
		// add all required listeners
		servletContext.addListener(new BridgeServletContextAttributeListener());
		servletContext.addListener(new BridgeHttpSessionListener());
		servletContext.addListener(new BridgeHttpSessionIdListener());
		servletContext.addListener(new BridgeHttpSessionAttributeListener());
	}
    
    private void registerProxyDispatcherServlet(ServletContext context, Logger logger) {
		// Register the ProxyDispatcherServlet after the OSGi Framework started successfully.
		// This will ensure that the FELIX {@link DispatcherServlet} is available as an OSGi service and can be tracked. 
		// ProxyDispatcherServlet delegates all the service calls to the FELIX DispatcherServlet.
		Dynamic registration = context.addServlet(PROXY_DISPATCHER_SERVLET, new ProxyDispatcherServlet());
		registration.addMapping(ROOT_MAPPING);
		// Required if [osgi.http.whiteboard.servlet.asyncSupported] is declared true for OSGi HttpService managed Servlets.
		// Otherwise the request processing fails throwing exception [java.lang.IllegalStateException: UT010026: 
		// Async is not supported for this request, as not all filters or Servlets were marked as supporting async]
		registration.setAsyncSupported(true);
		// Load early to detect any issue with OSGi FELIX DispatcherServlet initialization.
		// registration.setLoadOnStartup(0);
		logger.info("ProxyDispatcherServlet registered successfully!!");
	}

	private Framework createFramework(Logger logger) throws Exception {
		Framework framework = null;
		for (FrameworkFactory factory : ServiceLoader.load(FrameworkFactory.class)) {
			// There should only be a single FrameworkFactory.
			framework = factory.newFramework(this.createFrameworkConfigs(logger));
			break;
		}
		return framework;
	}

    private Map<String, String> createFrameworkConfigs(Logger logger) throws IOException {
        Map<String, String> configs = this.loadFrameworkProps();
        Config felixConf = Configs.INSTANCE.felix();
        configs.put("felix.cm.dir", felixConf.getString("felix-cm-dir"));
        configs.put("felix.memoryusage.dump.location", felixConf.getString("memoryusage-dump-loc"));
        logger.debug("OSGi Framework Configurations: {}", configs);
        return configs;
    }

    private Map<String, String> loadFrameworkProps() throws IOException {
		Properties props = new Properties();
        props.load(FrameworkProvisioner.class.getResourceAsStream(FRAMEWORK_PROPERTIES));
        Map<String, String> configs = new HashMap<>();
        props.forEach((key, val) -> {
        	configs.put((String) key, (String) val);
        });
		return configs;
	}
}