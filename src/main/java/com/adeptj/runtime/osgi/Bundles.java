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

import com.adeptj.runtime.common.OSGiUtils;
import com.adeptj.runtime.common.ServletContextHolder;
import com.adeptj.runtime.common.Times;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;

/**
 * Utility that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class Bundles {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bundles.class);

    private static final String BUNDLE_STARTED_MSG = "Started Bundle: [{}, Version: {}] in [{}] ms!";

    private static final String BUNDLE_PROVISIONED_MSG = "Provisioned [{}] Bundles in: [{}] ms!!";

    private static final String BENCHMARK_BUNDLE_START = "benchmark.bundle.start";

    /**
     * Static utility methods only.
     */
    private Bundles() {
    }

    /**
     * Provision the Bundles.
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
        AtomicInteger installCount = new AtomicInteger();
        BundleInstaller bundleInstaller = new BundleInstaller();
        bundleInstaller
                .installBundles(bundleInstaller.findBundles(bundlesDir), systemBundleContext)
                .peek(bundle -> installCount.incrementAndGet())
                .filter(OSGiUtils::isNotFragment)
                .forEach(Bundles::startBundle);
        LOGGER.info(BUNDLE_PROVISIONED_MSG, installCount.get(), Times.elapsedMillis(startTime));
    }

    private static void startBundle(Bundle bundle) {
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