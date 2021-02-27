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
import com.adeptj.runtime.common.OSGiUtil;
import com.adeptj.runtime.common.Times;
import com.typesafe.config.Config;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;

/**
 * Find, install and start the Bundles from given location using the System Bundle's BundleContext.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class BundleInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleInstaller.class);

    public static final String CFG_KEY_FELIX_CM_DIR = "felix-cm-dir";

    private static final String BUNDLE_STARTED_MSG = "Started Bundle [{}, Version: {}] in [{}] ms!";

    private static final String BUNDLE_PROVISIONED_MSG = "Provisioned [{}] Bundles in: [{}] ms!!";

    private static final String BENCHMARK_BUNDLE_START = "benchmark.bundle.start";

    private static final String SYS_PROP_PROVISION_BUNDLES_EXPLICITLY = "provision.bundles.explicitly";

    private static final String BUNDLES_DIR_REGEX = "^bundles.*\\.jar$";

    /**
     * Provision the Bundles.
     * <p>
     * Following happens in order.
     * 1. Collect Bundles
     * 2. Install Bundles
     * 3. Start Bundles
     *
     * @throws IOException exception thrown by provisioning mechanism.
     */
    void installAndStartBundles(Config felixConf) throws IOException {
        // config directory will not yet be created if framework is being provisioned first time.
        if (!Boolean.getBoolean(SYS_PROP_PROVISION_BUNDLES_EXPLICITLY)
                && Paths.get(felixConf.getString(CFG_KEY_FELIX_CM_DIR)).toFile().exists()) {
            LOGGER.info("As per configuration, bundles provisioning is skipped on server restart!!");
            return;
        }
        long startTime = System.nanoTime();
        LOGGER.info("Bundles provisioning start!!");
        AtomicInteger counter = new AtomicInteger(1); // add the system bundle to the total count
        this.collect(felixConf.getString(BUNDLES_ROOT_DIR_KEY))
                .map(url -> this.install(url, counter))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Bundle::getBundleId)) // start bundles in ascending order of bundle id.
                .filter(OSGiUtil::isNotFragment)
                .forEach(this::start);
        LOGGER.info(BUNDLE_PROVISIONED_MSG, counter.get(), Times.elapsedMillis(startTime));
    }

    private Stream<URL> collect(String bundlesDir) throws IOException {
        Pattern pattern = Pattern.compile(BUNDLES_DIR_REGEX);
        ClassLoader cl = this.getClass().getClassLoader();
        URL resource = cl.getResource(bundlesDir);
        if (resource == null) {
            throw new IllegalStateException(String.format("Could not obtain bundles from location [%s]", bundlesDir));
        }
        // Will the cast be successful on other JVMs? Not doing a type check because we need a JarURLConnection.
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        return connection.getJarFile()
                .stream()
                .filter(jarEntry -> pattern.matcher(jarEntry.getName()).matches())
                .map(jarEntry -> cl.getResource(jarEntry.getName()))
                .filter(Objects::nonNull);
    }

    private Bundle install(URL url, AtomicInteger counter) {
        LOGGER.debug("Installing Bundle from location: [{}]", url);
        Bundle bundle = null;
        try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
            if (OSGiUtil.isNotBundle(jis.getManifest())) {
                LOGGER.error("Artifact [{}] is not a Bundle, skipping install!!", url);
            } else {
                bundle = BundleContextHolder.getInstance().getBundleContext().installBundle(url.toString());
                counter.getAndIncrement();
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing Bundle: [{}]. Cause:", url, ex);
        }
        return bundle;
    }

    private void start(Bundle bundle) {
        try {
            if (Boolean.getBoolean(BENCHMARK_BUNDLE_START)) {
                long startTime = System.nanoTime();
                bundle.start();
                LOGGER.info(BUNDLE_STARTED_MSG, bundle, bundle.getVersion(), Times.elapsedMillis(startTime));
            } else {
                bundle.start();
                LOGGER.info("Started Bundle: [{}, Version: {}]", bundle, bundle.getVersion());
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while starting Bundle: [{}, Version: {}]", bundle, bundle.getVersion(), ex);
        }
    }
}
