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

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * FrameworkProvisioner that handles the OSGi Framework startup and shutdown.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkProvisioner {

    INSTANCE;

    public static final String SHARED_SERVLET_CONTEXT_ATTRS = "org.apache.felix.http.shared_servlet_context_attributes";

	private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

	public static final String DISPATCHER = "ProxyDispatcherServlet";

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
            ServletContext context = ServletContextAware.INSTANCE.getServletContext();
            context.setAttribute(BundleContext.class.getName(), this.systemBundleContext);
            BundleProvisioner.INSTANCE.provisionBundles(this.systemBundleContext);
            LOGGER.info("OSGi Framework started in [{}] ms!!", (System.currentTimeMillis() - startTime));
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
    
    private void registerProxyDispatcherServlet(ProxyDispatcherServlet servlet, ServletContext context) {
		/*
		 * Register the ProxyDispatcherServlet after the OSGi Framework started successfully.
		 * This will ensure that the Felix {@link DispatcherServlet} is available as an OSGi service and can be tracked. 
		 * {@link ProxyDispatcherServlet} collect the DispatcherServlet service and delegates all the service calls to it.
		 */
		Dynamic registration = context.addServlet(DISPATCHER, servlet);
		registration.addMapping(ROOT_MAPPING);
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

    private Map<String, String> createFrameworkConfigs() throws Exception {
        Properties props = new Properties();
        props.load(FrameworkProvisioner.class.getResourceAsStream(FRAMEWORK_PROPERTIES));
        Map<String, String> configs = new HashMap<>();
        props.forEach((key, val) -> {
        	configs.put((String) key, (String) val);
        });
		/*
		 * WARNING: Only set this property if absolutely needed.
		 * (for example you implement an HttpSessionListener and want to access {@ link ServletContext}
		 * attributes of the ServletContext to which the HttpSession is linked). Otherwise leave this property unset.
		 */
        Config felix = Configs.INSTANCE.main().getConfig("felix");
        configs.put(SHARED_SERVLET_CONTEXT_ATTRS, felix.getString("shared-servlet-context-attributes"));
        String frameworkArtifactsDir = System.getProperty("user.dir") + File.separator + "modularweb-micro";
        configs.put("org.osgi.framework.storage", frameworkArtifactsDir + File.separator + "osgi-bundles");
        configs.put("felix.cm.dir", frameworkArtifactsDir + File.separator + "osgi-configs");
        configs.put("felix.memoryusage.dump.location", frameworkArtifactsDir + File.separator + "heap-dumps");
        configs.put("org.osgi.framework.bundle.parent", felix.getString("framework-bundle-parent"));
        // set felix.log.level debug
        String felixLogProp = System.getProperty("felix.log.level");
        if (felixLogProp != null && !felixLogProp.isEmpty()) {
        	configs.put("felix.log.level", felixLogProp);
        } else {
			configs.put("felix.log.level", felix.getString("felix-config-log"));
        }
        /*
         * WARNING: This breaks OSGi Modularity, But EhCache won't work without this.
         * Declaring on Sun specific classes only.
         */
        configs.put("org.osgi.framework.bootdelegation", felix.getString("osgi-bootdelegation"));
		/*
		 * Register the OsgiManager HttpServlet using the prefix "/" so that it could be resolved by Felix DispatcherServlet
		 * which is registered on "/" itself. This is optional as "/system/console" is default.
		 */
        configs.put("felix.webconsole.manager.root", "/system/console");
        LOGGER.debug("OSGi Framework Configurations: {}", configs);
        return configs;
    }
}