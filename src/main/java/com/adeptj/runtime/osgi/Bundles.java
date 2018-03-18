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

import com.adeptj.runtime.common.ServletContextHolder;
import com.adeptj.runtime.common.Times;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;
import static org.osgi.framework.Constants.FRAGMENT_HOST;

/**
 * Utility that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class Bundles {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bundles.class);

    private static final String BUNDLE_STARTED_MSG = "Bundle: [{}, Version: {}] started in [{}] ms!";

    /**
     * Static utility methods only.
     */
    private Bundles() {
    }

    /**
     * Provision the bundles.
     * <p>
     * Following happens in order.
     * 1. Find Bundles
     * 2. Install Bundles
     * 3. Start Bundles
     *
     * @param systemBundleContext the {@link BundleContext} of system bundle.
     * @throws IOException exception thrown by provisioning mechanism.
     */
    static void provisionBundles(BundleContext systemBundleContext) throws IOException {
        long startTime = System.nanoTime();
        String bundlesDir = ServletContextHolder.INSTANCE.getServletContext().getInitParameter(BUNDLES_ROOT_DIR_KEY);
        BundleInstaller bundleInstaller = new BundleInstaller();
        List<URL> bundleUrls = bundleInstaller.findBundles(bundlesDir);
        List<Bundle> installedBundles = bundleInstaller.installBundles(bundleUrls, systemBundleContext);
        startBundles(installedBundles);
        LOGGER.info("Provisioning of Bundles took: [{}] ms!!", Times.elapsedMillis(startTime));
    }

    private static void startBundles(List<Bundle> bundles) {
        // Fragment Bundles can't be started so put a check for [Fragment-Host] header.
        boolean benchmarkBundleStart = Boolean.getBoolean("benchmark.bundle.start");
        bundles.stream()
                .filter(bundle -> bundle.getHeaders().get(FRAGMENT_HOST) == null)
                .forEach(bundle -> {
                    if (benchmarkBundleStart) {
                        benchmarkBundleStart(bundle);
                    } else {
                        startBundle(bundle);
                    }
                });
    }

    private static void startBundle(Bundle bundle) {
        try {
            bundle.start();
            LOGGER.info("Bundle: [{}, Version: {}] started!", bundle, bundle.getVersion());
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while starting Bundle: [{}]. Cause:", bundle, ex);
        }
    }

    private static void benchmarkBundleStart(Bundle bundle) {
        try {
            long startTime = System.nanoTime();
            bundle.start();
            LOGGER.info(BUNDLE_STARTED_MSG, bundle, bundle.getVersion(), Times.elapsedMillis(startTime));
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while starting Bundle: [{}]. Cause:", bundle, ex);
        }
    }
}