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
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;
import static org.osgi.framework.Constants.FRAGMENT_HOST;

/**
 * Utility that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class Bundles {

    private static final String PREFIX_BUNDLES = "bundles/";

    private static final String JAR_FILE = ".jar";

    /**
     * Don't let anyone instantiate this class.
     */
    private Bundles() {
    }

    static void provisionBundles(BundleContext systemBundleContext) throws IOException {
        long startTime = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(Bundles.class);
        String rootPath = ServletContextHolder.INSTANCE.getServletContext().getInitParameter(BUNDLES_ROOT_DIR_KEY);
        // Following happens in order.
        // 1. Collect Bundles
        // 2. Install Bundles
        // 3. Start Bundles
        startBundles(installBundles(collectBundles(rootPath), systemBundleContext, logger), logger);
        logger.info("Provisioning of Bundles took: [{}] ms!!", Times.elapsedSinceMillis(startTime));
    }

    private static void startBundles(List<Bundle> bundles, Logger logger) {
        // Fragment Bundles can't be started so put a check for [Fragment-Host] header.
        bundles.stream()
                .filter(bundle -> bundle.getHeaders().get(FRAGMENT_HOST) == null)
                .forEach(bundle -> startBundle(bundle, logger));
    }

    private static void startBundle(Bundle bundle, Logger logger) {
        logger.info("Starting bundle: [{}], version: [{}]", bundle, bundle.getVersion());
        try {
            bundle.start();
        } catch (Exception ex) { // NOSONAR
            logger.error("Exception while starting bundle: [{}]. Cause:", bundle, ex);
        }
    }

    private static List<Bundle> installBundles(List<URL> bundles, BundleContext context, Logger logger) {
        // First install all the Bundles.
        List<Bundle> installedBundles = bundles
                .stream()
                .map(url -> installBundle(url, context, logger))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        logger.info("Total Bundles installed, excluding System Bundle: [{}]", installedBundles.size());
        return installedBundles;
    }

    private static Bundle installBundle(URL url, BundleContext context, Logger logger) {
        logger.debug("Installing Bundle from location: [{}]", url);
        Bundle bundle = null;
        try {
            bundle = context.installBundle(url.toExternalForm());
        } catch (BundleException | IllegalStateException | SecurityException ex) {
            logger.error("Exception while installing bundle: [{}]. Cause:", url, ex);
        }
        return bundle;
    }

    private static List<URL> collectBundles(String rootPath) throws IOException {
        return JarURLConnection.class.cast(Bundles.class.getResource(rootPath)
                .openConnection())
                .getJarFile()
                .stream()
                .filter(jarEntry -> StringUtils.startsWith(jarEntry.getName(), PREFIX_BUNDLES)
                        && StringUtils.endsWith(jarEntry.getName(), JAR_FILE))
                .map(jarEntry -> Bundles.class.getClassLoader().getResource(jarEntry.getName()))
                .collect(Collectors.toList());
    }
}