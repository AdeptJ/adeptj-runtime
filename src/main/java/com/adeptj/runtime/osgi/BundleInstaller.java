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
import java.util.Comparator;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find and Install the Bundles from given location using the System Bundle's BundleContext.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class BundleInstaller {

    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";

    private static final Pattern PATTERN_BUNDLE = Pattern.compile("^bundles.*\\.jar$");

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleInstaller.class);

    Stream<JarEntry> findBundles(String bundlesDir) throws IOException {
        return JarURLConnection.class.cast(Bundles.class.getResource(bundlesDir).openConnection())
                .getJarFile()
                .stream()
                .filter(jarEntry -> PATTERN_BUNDLE.matcher(jarEntry.getName()).matches());
    }

    Stream<Bundle> installBundles(Stream<JarEntry> jarEntryStream, BundleContext systemBundleContext) {
        return jarEntryStream
                .map(jarEntry -> this.getClass().getClassLoader().getResource(jarEntry.getName()))
                .map(url -> this.installBundle(url, systemBundleContext))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Bundle::getBundleId));
    }

    private Bundle installBundle(URL bundleUrl, BundleContext systemBundleContext) {
        LOGGER.debug("Installing Bundle from location: [{}]", bundleUrl);
        Bundle bundle = null;
        try (JarInputStream jar = new JarInputStream(bundleUrl.openStream(), false)) {
            if (StringUtils.isEmpty(jar.getManifest().getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME))) {
                LOGGER.warn("Not an OSGi Bundle: {}", bundleUrl);
            } else {
                bundle = systemBundleContext.installBundle(bundleUrl.toExternalForm());
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing Bundle: [{}]. Cause:", bundleUrl, ex);
        }
        return bundle;
    }
}
