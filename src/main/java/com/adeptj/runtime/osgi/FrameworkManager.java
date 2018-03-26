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
import com.adeptj.runtime.common.Environment;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.servlet.osgi.PerServletContextErrorServlet;
import com.typesafe.config.Config;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import static java.util.Optional.ofNullable;

/**
 * FrameworkManager: Handles the OSGi Framework(Apache Felix) lifecycle such as startup, shutdown etc.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkManager {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkManager.class);

    private static final String FRAMEWORK_PROPERTIES = "/framework.properties";

    private static final String FELIX_CM_DIR = "felix.cm.dir";

    private static final String CFG_KEY_FELIX_CM_DIR = "felix-cm-dir";

    private static final String MEM_DUMP_LOC = "felix.memoryusage.dump.location";

    private static final String CFG_KEY_MEM_DUMP_LOC = "memoryusage-dump-loc";

    private static final String FELIX_LOG_LEVEL = "felix.log.level";

    private Framework framework;

    private FrameworkRestartHandler frameworkListener;

    public void startFramework() {
        try {
            long startTime = System.nanoTime();
            LOGGER.info("Starting the OSGi Framework!!");
            FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
            this.framework = frameworkFactory.newFramework(this.frameworkConfigs());
            long startTimeFramework = System.nanoTime();
            this.framework.start();
            LOGGER.info("OSGi Framework creation took [{}] ms!!", Times.elapsedMillis(startTimeFramework));
            BundleContext systemBundleContext = this.framework.getBundleContext();
            this.frameworkListener = new FrameworkRestartHandler();
            systemBundleContext.addFrameworkListener(this.frameworkListener);
            BundleContextHolder.INSTANCE.setBundleContext(systemBundleContext);
            this.provisionBundles(systemBundleContext);
            OSGiServlets.INSTANCE.registerErrorServlet(systemBundleContext, new PerServletContextErrorServlet(),
                    Configs.DEFAULT.undertow().getStringList("common.osgi-error-pages"));
            LOGGER.info("OSGi Framework started in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Failed to start OSGi Framework!!", ex);
            // Stop the Framework if the Bundles throws exception.
            this.stopFramework();
        }
    }

    public void stopFramework() {
        try {
            if (this.framework != null) {
                if (BundleContextHolder.INSTANCE.isBundleContextAvailable()) {
                    BundleContextHolder.INSTANCE.getBundleContext().removeFrameworkListener(this.frameworkListener);
                }
                OSGiServlets.INSTANCE.unregisterAll();
                this.framework.stop();
                // A value of zero will wait indefinitely.
                FrameworkEvent event = this.framework.waitForStop(0);
                LOGGER.info("OSGi FrameworkEvent: [{}]", FrameworkEvents.asString(event.getType())); // NOSONAR
            } else {
                LOGGER.info("OSGi Framework not started yet, nothing to stop!!");
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Error Stopping OSGi Framework!!", ex);
        }
    }

    private void provisionBundles(BundleContext systemBundleContext) throws IOException {
        // config directory will not yet be created if framework is being provisioned first time.
        if (Paths.get(Configs.DEFAULT.felix().getString(CFG_KEY_FELIX_CM_DIR)).toFile().exists()
                && !Boolean.getBoolean("provision.bundles.explicitly")) {
            LOGGER.info("Bundles already provisioned, this must be a server restart!!");
        } else {
            LOGGER.info("Provisioning Bundles first time!!");
            Bundles.provisionBundles(systemBundleContext);
        }
    }

    private Map<String, String> frameworkConfigs() {
        Map<String, String> configs = this.loadFrameworkProperties();
        Config felixConf = Configs.DEFAULT.felix();
        configs.put(FELIX_CM_DIR, felixConf.getString(CFG_KEY_FELIX_CM_DIR));
        configs.put(MEM_DUMP_LOC, felixConf.getString(CFG_KEY_MEM_DUMP_LOC));
        ofNullable(System.getProperty(FELIX_LOG_LEVEL)).ifPresent(level -> configs.put(FELIX_LOG_LEVEL, level));
        LOGGER.debug("OSGi Framework Configurations: {}", configs);
        return configs;
    }

    private Map<String, String> loadFrameworkProperties() {
        Properties props = new Properties();
        Map<String, String> configs = new HashMap<>();
        if (Environment.isFrameworkConfFileExists()) {
            try (InputStream is = Files.newInputStream(Environment.getFrameworkConfPath())) {
                props.load(is);
                props.forEach((key, val) -> configs.put((String) key, (String) val));
            } catch (IOException ex) {
                LOGGER.error("IOException while loading framework.properties from file system!!", ex);
                // Fallback is try to load the classpath framework.properties
                this.loadClasspathFrameworkProperties(props, configs);
            }
        } else {
            this.loadClasspathFrameworkProperties(props, configs);
            this.createFrameworkPropertiesFile();
        }
        return configs;
    }

    private void loadClasspathFrameworkProperties(Properties props, Map<String, String> configs) {
        try (InputStream is = FrameworkManager.class.getResourceAsStream(FRAMEWORK_PROPERTIES)) {
            props.load(is);
            props.forEach((key, val) -> configs.put((String) key, (String) val));
        } catch (IOException exception) {
            LOGGER.error("IOException while loading framework.properties from classpath!!", exception);
        }
    }

    private void createFrameworkPropertiesFile() {
        try (InputStream is = FrameworkManager.class.getResourceAsStream(FRAMEWORK_PROPERTIES)) {
            Files.write(Environment.getFrameworkConfPath(), IOUtils.toByteArray(is));
        } catch (IOException ex) {
            LOGGER.error("IOException!!", ex);
        }
    }
}