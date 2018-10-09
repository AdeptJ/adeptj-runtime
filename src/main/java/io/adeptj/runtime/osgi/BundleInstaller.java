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

import io.adeptj.runtime.common.BundleContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final AtomicInteger installationCount = new AtomicInteger();

    int getInstallationCount() {
        return this.installationCount.get() + 1; // add the system bundle to the total count.
    }

    Stream<Bundle> install(ClassLoader cl, String bundlesDir) throws IOException {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Pattern pattern = Pattern.compile("^bundles.*\\.jar$");
        return ((JarURLConnection) this.getClass().getResource(bundlesDir).openConnection())
                .getJarFile()
                .stream()
                .filter(jarEntry -> pattern.matcher(jarEntry.getName()).matches())
                .map(jarEntry -> cl.getResource(jarEntry.getName()))
                .filter(Objects::nonNull)
                .map(url -> {
                    logger.debug("Installing Bundle from location: [{}]", url);
                    Bundle bundle = null;
                    try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
                        if (StringUtils.isEmpty(jis.getManifest().getMainAttributes().getValue(BUNDLE_SYMBOLIC_NAME))) {
                            logger.warn("Artifact [{}] is not a Bundle, skipping install!!", url);
                        } else {
                            bundle = BundleContextHolder.getInstance().getBundleContext().installBundle(url.toExternalForm());
                            this.installationCount.incrementAndGet();
                        }
                    } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
                        logger.error("Exception while installing Bundle: [{}]. Cause:", url, ex);
                    }
                    return bundle;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(Bundle::getBundleId));
    }
}
