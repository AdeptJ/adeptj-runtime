/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
*/
package com.adeptj.modularweb.micro.osgi;

import java.io.File;
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

import com.adeptj.modularweb.micro.common.ServletContextAware;
import com.adeptj.modularweb.micro.config.Configs;
import com.adeptj.modularweb.micro.servlet.ProxyDispatcherServlet;
import com.typesafe.config.Config;

/**
 * FrameworkProvisioner that handles the OSGi Framework startup and shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkProvisioner {

    INSTANCE;

    public static final String SHARED_SERVLET_CONTEXT_ATTRS = "org.apache.felix.http.shared_servlet_context_attributes";

	private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

	public static final String PROXY_DISPATCHER_SERVLET = "ProxyDispatcherServlet";

	public static final String ROOT_MAPPING = "/*";

	private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkProvisioner.class);

    private Framework framework;

    private FrameworkRestartHandler listener;

    private BundleContext systemBundleContext;

    public void startFramework() {
        try {
        	LOGGER.info("Starting the OSGi Framework!!");
    		long startTime = System.currentTimeMillis();
            this.framework = this.createFramework();
            this.framework.start();
            this.systemBundleContext = this.framework.getBundleContext();
            ProxyDispatcherServlet proxyDispatcherServlet = new ProxyDispatcherServlet();
            this.listener = new FrameworkRestartHandler(proxyDispatcherServlet);
            this.systemBundleContext.addFrameworkListener(this.listener);
            // Set the BundleContext as a ServletContext attribute as per Felix HttpBridge Specification.
            BundleProvisioner.INSTANCE.provisionBundles(this.systemBundleContext);
            LOGGER.info("OSGi Framework started in [{}] ms!!", (System.currentTimeMillis() - startTime));
            ServletContext context = ServletContextAware.INSTANCE.getServletContext();
            this.initBridgeListeners(context);
            context.setAttribute(BundleContext.class.getName(), this.systemBundleContext);
            this.registerProxyDispatcherServlet(proxyDispatcherServlet, context);
        } catch (Exception ex) {
            LOGGER.error("Failed to start OSGi Framework!!", ex);
            // Stop the Framework if the BundleProvisioner throws exception.
            this.stopFramework();
        }
    }

    protected void setSystemBundleContext(BundleContext bundleContext) {
    	this.systemBundleContext = bundleContext;
    }

    public void stopFramework() {
        try {
        	if (this.framework != null && this.systemBundleContext != null) {
        		this.systemBundleContext.removeFrameworkListener(this.listener);
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
    
    /**
	 * Adds the following to ServletContext.
	 * 
	 * HttpSessionListener
	 * 
	 * HttpSessionIdListener
	 * 
	 * HttpSessionAttributeListener
	 * 
	 * ServletContextAttributeListener
	 * 
	 */
	public void initBridgeListeners(ServletContext servletContext) {
		// add all required listeners
		servletContext.addListener(new BridgeServletContextAttributeListener());
		servletContext.addListener(new BridgeHttpSessionListener());
		servletContext.addListener(new BridgeHttpSessionIdListener());
		servletContext.addListener(new BridgeHttpSessionAttributeListener());
	}
    
    private void registerProxyDispatcherServlet(ProxyDispatcherServlet servlet, ServletContext context) {
		// Register the ProxyDispatcherServlet after the OSGi Framework started successfully.
		// This will ensure that the FELIX {@link DispatcherServlet} is available as an OSGi service and can be tracked. 
		// ProxyDispatcherServlet delegates all the service calls to the FELIX DispatcherServlet.
		Dynamic registration = context.addServlet(PROXY_DISPATCHER_SERVLET, servlet);
		registration.addMapping(ROOT_MAPPING);
		// Load early to detect any issue with OSGi FELIX DispatcherServlet initialization.
		registration.setLoadOnStartup(1);
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
        Config felix = Configs.INSTANCE.main().getConfig("felix");
        configs.put(SHARED_SERVLET_CONTEXT_ATTRS, felix.getString("shared-servlet-context-attributes"));
        String frameworkArtifactsDir = System.getProperty("user.dir") + File.separator + "modularweb-micro";
        configs.put("org.osgi.framework.storage", frameworkArtifactsDir + File.separator + "osgi-bundles");
        configs.put("felix.cm.dir", frameworkArtifactsDir + File.separator + "osgi-configs");
        configs.put("felix.memoryusage.dump.location", frameworkArtifactsDir + File.separator + "heap-dumps");
        configs.put("org.osgi.framework.bundle.parent", felix.getString("framework-bundle-parent"));
        String felixLogLevel = System.getProperty("felix.log.level");
        if (felixLogLevel == null || felixLogLevel.isEmpty()) {
        	configs.put("felix.log.level", felix.getString("felix-config-log"));
        } else {
        	configs.put("felix.log.level", felixLogLevel);
        }
        // WARNING: This breaks OSGi Modularity, But EhCache and some other modules won't work without this.
        configs.put("org.osgi.framework.bootdelegation", felix.getString("osgi-bootdelegation"));
        configs.put("felix.webconsole.manager.root", "/system/console");
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