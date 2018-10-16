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

package io.adeptj.runtime.osgi;

import io.adeptj.runtime.common.OSGiUtil;
import io.adeptj.runtime.common.Times;
import io.adeptj.runtime.config.Configs;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;

/**
 * Utility that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class Bundles {

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
     * @throws IOException exception thrown by provisioning mechanism.
     */
    static void provisionBundles() throws IOException {
        long startTime = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(Bundles.class);
        logger.info("Bundles provisioning start!!");
        BundleInstaller installer = new BundleInstaller();
        installer.install(Bundles.class.getClassLoader(), Configs.of().common().getString(BUNDLES_ROOT_DIR_KEY))
                .filter(OSGiUtil::isNotFragment)
                .forEach(Bundles::startBundle);
        logger.info(BUNDLE_PROVISIONED_MSG, installer.getInstallationCount(), Times.elapsedMillis(startTime));
    }

    private static void startBundle(Bundle bundle) {
        Logger logger = LoggerFactory.getLogger(Bundles.class);
        try {
            if (Boolean.getBoolean(BENCHMARK_BUNDLE_START)) {
                long startTime = System.nanoTime();
                bundle.start();
                logger.info(BUNDLE_STARTED_MSG, bundle, bundle.getVersion(), Times.elapsedMillis(startTime));
            } else {
                bundle.start();
                logger.info("Started Bundle: [{}, Version: {}]", bundle, bundle.getVersion());
            }
        } catch (Exception ex) { // NOSONAR
            logger.error("Exception while starting Bundle: [{}, Version: {}]", bundle, bundle.getVersion(), ex);
        }
    }
}