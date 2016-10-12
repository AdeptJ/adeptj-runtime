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
package com.adeptj.modularweb.micro.osgi;

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

import com.adeptj.modularweb.micro.common.BundleContextAware;
import com.adeptj.modularweb.micro.config.Configs;
import com.adeptj.modularweb.micro.servlet.ProxyDispatcherServlet;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkProvisioner.class);

    private Framework framework;

    private FrameworkRestartHandler frameworkListener;

    public void startFramework(ServletContext context) {
        try {
        	LOGGER.info("Starting the OSGi Framework!!");
    		long startTime = System.currentTimeMillis();
            this.framework = this.createFramework();
            this.framework.start();
            this.frameworkListener = new FrameworkRestartHandler();
            BundleContext systemBundleContext = this.framework.getBundleContext();
            systemBundleContext.addFrameworkListener(this.frameworkListener);
            BundleContextAware.INSTANCE.setBundleContext(systemBundleContext);
            BundleProvisioner.INSTANCE.provisionBundles(systemBundleContext);
            LOGGER.info("OSGi Framework started in [{}] ms!!", (System.currentTimeMillis() - startTime));
            this.initBridgeListeners(context);
            // Set the BundleContext as a ServletContext attribute as per FELIX HttpBridge Specification.
            context.setAttribute(BundleContext.class.getName(), systemBundleContext);
            this.registerProxyDispatcherServlet(context);
        } catch (Exception ex) {
            LOGGER.error("Failed to start OSGi Framework!!", ex);
            // Stop the Framework if the BundleProvisioner throws exception.
            this.stopFramework();
        }
    }

    public void stopFramework() {
        try {
        	if (this.framework != null) {
        		BundleContext bundleContext = BundleContextAware.INSTANCE.getBundleContext();
        		if (bundleContext != null) {
        			bundleContext.removeFrameworkListener(this.frameworkListener);	
        		}
                this.framework.stop();
                // A value of zero will wait indefinitely.
                FrameworkEvent event = this.framework.waitForStop(0);
                LOGGER.info("OSGi Framework Stopped, Event Code: [{}]", event.getType());
        	} else {
        		LOGGER.info("OSGi Framework not started yet, nothing to stop!!");
        	}
        } catch (Exception ex) {
            LOGGER.error("Error Stopping OSGi Framework!!", ex);
        }
    }
    
	private void initBridgeListeners(ServletContext servletContext) {
		// add all required listeners
		servletContext.addListener(new BridgeServletContextAttributeListener());
		servletContext.addListener(new BridgeHttpSessionListener());
		servletContext.addListener(new BridgeHttpSessionIdListener());
		servletContext.addListener(new BridgeHttpSessionAttributeListener());
	}
    
    private void registerProxyDispatcherServlet(ServletContext context) {
		// Register the ProxyDispatcherServlet after the OSGi Framework started successfully.
		// This will ensure that the FELIX {@link DispatcherServlet} is available as an OSGi service and can be tracked. 
		// ProxyDispatcherServlet delegates all the service calls to the FELIX DispatcherServlet.
		Dynamic registration = context.addServlet(PROXY_DISPATCHER_SERVLET, new ProxyDispatcherServlet());
		registration.addMapping(ROOT_MAPPING);
		// Load early to detect any issue with OSGi FELIX DispatcherServlet initialization.
		registration.setLoadOnStartup(0);
		LOGGER.info("ProxyDispatcherServlet registered successfully!!");
	}

	private Framework createFramework() throws Exception {
		Framework framework = null;
		for (FrameworkFactory factory : ServiceLoader.load(FrameworkFactory.class, this.getClass().getClassLoader())) {
			framework = factory.newFramework(this.createFrameworkConfigs());
			// Ideally there will only be a single FrameworkFactory.
			break;
		}
		return framework;
	}

    private Map<String, String> createFrameworkConfigs() throws IOException {
        Map<String, String> configs = this.loadFrameworkProps();
        Config felixConf = Configs.INSTANCE.main().getConfig("felix");
        configs.put("felix.cm.dir", felixConf.getString("felix-cm-dir"));
        configs.put("felix.memoryusage.dump.location", felixConf.getString("memoryusage-dump-loc"));
        LOGGER.debug("OSGi Framework Configurations: {}", configs);
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