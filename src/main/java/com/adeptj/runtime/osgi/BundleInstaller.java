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

import com.adeptj.runtime.common.OSGiUtil;
import com.adeptj.runtime.common.Times;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;

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

    private static final String DOT_JAR = ".jar";

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
    boolean installAndStartBundles(Config felixConf, BundleContext bundleContext) throws IOException {
        // config directory will not yet be created if framework is being provisioned first time.
        File frameworkConfigDir = Paths.get(felixConf.getString(CFG_KEY_FELIX_CM_DIR)).toFile();
        if (frameworkConfigDir.exists()) {
            if (Boolean.getBoolean(SYS_PROP_PROVISION_BUNDLES_EXPLICITLY)) {
                // Update
                return this.handleUpdate(felixConf, bundleContext);
            }
            // Restart, just return.
            return false;
        }
        // Install
        long startTime = System.nanoTime();
        LOGGER.info("Bundles provisioning start!!");
        AtomicInteger counter = new AtomicInteger(1); // add the system bundle to the total count
        this.collect(felixConf.getString(BUNDLES_ROOT_DIR_KEY))
                .map(url -> this.install(url, bundleContext, counter))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Bundle::getBundleId)) // start bundles in ascending order of bundle id.
                .filter(OSGiUtil::isNotFragment)
                .forEach(this::start);
        LOGGER.info(BUNDLE_PROVISIONED_MSG, counter.get(), Times.elapsedMillis(startTime));
        return false;
    }

    private Stream<URL> collect(String bundlesDir) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        return this.getJarFile(bundlesDir, cl)
                .stream()
                .filter(jarEntry -> this.isJarEntryFromBundlesDir(jarEntry, bundlesDir))
                .map(jarEntry -> cl.getResource(jarEntry.getName()))
                .filter(Objects::nonNull);
    }

    private List<URL> collectAsList(String bundlesDir) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        List<URL> bundles = new ArrayList<>();
        Enumeration<JarEntry> entries = this.getJarFile(bundlesDir, cl).entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (this.isJarEntryFromBundlesDir(jarEntry, bundlesDir)) {
                URL bundle = cl.getResource(jarEntry.getName());
                if (bundle != null) {
                    bundles.add(bundle);
                }
            }
        }
        return bundles;
    }

    private JarFile getJarFile(String bundlesDir, ClassLoader cl) throws IOException {
        URL resource = cl.getResource(bundlesDir);
        if (resource == null) {
            throw new IllegalStateException(String.format("Could not obtain bundles from location [%s]", bundlesDir));
        }
        // Will the cast be successful on other JVMs? Not doing a type check because we need a JarURLConnection.
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        return connection.getJarFile();
    }

    private boolean isJarEntryFromBundlesDir(JarEntry jarEntry, String bundlesDir) {
        String name = jarEntry.getName();
        return name.startsWith(bundlesDir) && name.endsWith(DOT_JAR);
    }

    private Bundle install(URL url, BundleContext bundleContext, AtomicInteger counter) {
        LOGGER.debug("Installing Bundle from location: [{}]", url);
        Bundle bundle = null;
        try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
            if (OSGiUtil.isNotBundle(jis.getManifest())) {
                LOGGER.error("Artifact [{}] is not a Bundle, skipping install!!", url);
            } else {
                bundle = bundleContext.installBundle(url.toExternalForm());
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

    private boolean handleUpdate(Config felixConf, BundleContext bundleContext) throws IOException {
        boolean restartFramework = false;
        Map<String, Bundle> bundles = Stream.of(bundleContext.getBundles())
                .collect(Collectors.toMap(Bundle::getSymbolicName, bundle -> bundle));
        List<Bundle> newBundles = new ArrayList<>();
        for (URL url : this.collectAsList(felixConf.getString(BUNDLES_ROOT_DIR_KEY))) {
            restartFramework |= this.doHandleUpdate(url, bundles, newBundles, bundleContext);
        }
        // start the newly installed bundles.
        for (Bundle bundle : newBundles) {
            if (bundle != null && OSGiUtil.isNotFragment(bundle)) {
                this.start(bundle);
            }
        }
        return restartFramework;
    }

    private boolean doHandleUpdate(URL url, Map<String, Bundle> bundles,
                                   List<Bundle> newBundles, BundleContext bundleContext) {
        boolean restartFramework = false;
        try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
            Manifest manifest = jis.getManifest();
            if (manifest == null) {
                LOGGER.error("Manifest missing for url: {}, skipping it!!", url);
                return false;
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            String symbolicName = mainAttributes.getValue(BUNDLE_SYMBOLICNAME);
            if (StringUtils.isEmpty(symbolicName)) {
                LOGGER.error("Artifact [{}] is not a Bundle, skipping it!!", url);
                return false;
            }
            Bundle installedBundle = bundles.get(symbolicName);
            if (installedBundle == null) {
                // Install
                newBundles.add(bundleContext.installBundle(url.toExternalForm()));
                return false;
            }
            // Update - only when new bundle has higher version than the installed one.
            Version newVersion = OSGiUtil.getBundleVersion(mainAttributes);
            Version installedVersion = OSGiUtil.getBundleVersion(installedBundle);
            if (newVersion.compareTo(installedVersion) > 0) {
                restartFramework = OSGiUtil.isSystemBundleFragment(installedBundle);
                installedBundle.update(url.openStream());
                LOGGER.info("Updated bundle: {}", installedBundle);
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing Bundle: [{}]. Cause:", url, ex);
        }
        return restartFramework;
    }
}
