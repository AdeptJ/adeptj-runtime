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
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Find and Install the Bundles from given location using the System Bundle's BundleContext.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class BundleInstaller {

    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

    private static final String PATTERN_BUNDLE = "^bundles.*\\.jar$";

    private final Logger logger = LoggerFactory.getLogger(BundleInstaller.class);

    List<URL> findBundles(String bundlesDir) throws IOException {
        Pattern pattern = Pattern.compile(PATTERN_BUNDLE);
        ClassLoader classLoader = Bundles.class.getClassLoader();
        return JarURLConnection.class.cast(Bundles.class.getResource(bundlesDir).openConnection())
                .getJarFile()
                .stream()
                .filter(jarEntry -> pattern.matcher(jarEntry.getName()).matches())
                .map(jarEntry -> classLoader.getResource(jarEntry.getName()))
                .collect(Collectors.toList());
    }

    List<Bundle> installBundles(List<URL> bundleUrls, BundleContext systemBundleContext) {
        List<Bundle> installedBundles = bundleUrls
                .stream()
                .map(url -> this.installBundle(url, systemBundleContext))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.logger.info("Total Bundles installed, excluding System Bundle: [{}]", installedBundles.size());
        return installedBundles;
    }

    private Bundle installBundle(URL bundleUrl, BundleContext systemBundleContext) {
        this.logger.debug("Installing Bundle from location: [{}]", bundleUrl);
        Bundle bundle = null;
        try (JarInputStream jar = new JarInputStream(bundleUrl.openStream(), false)) {
            if (StringUtils.isEmpty(jar.getManifest().getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME))) {
                this.logger.warn("Not a Bundle: {}", bundleUrl.toExternalForm());
            } else {
                bundle = systemBundleContext.installBundle(bundleUrl.toExternalForm());
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            this.logger.error("Exception while installing Bundle: [{}]. Cause:", bundleUrl, ex);
        }
        return bundle;
    }
}
