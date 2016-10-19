/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, Rakesh Kumar, AdeptJ (http://adeptj.com)                 #
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
package com.adeptj.modularweb.micro.osgi;

import static com.adeptj.modularweb.micro.common.Constants.BUNDLES_ROOT_DIR_KEY;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.common.ServletContextAware;

/**
 * BundleProvisioner that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class BundleProvisioner {
	
	// No instantiation.
	private BundleProvisioner() {}
	
	private static final String BUNDLES_JAR_DIR = "bundles/";
	
	private static final String HEADER_FRAGMENT_HOST = "Fragment-Host";

	private static final String EXTN_JAR = ".jar";

	public static void provisionBundles(BundleContext systemBundleContext) throws Exception {
		Logger logger = LoggerFactory.getLogger(BundleProvisioner.class);
		// Now start all the installed Bundles.
		startBundles(installBundles(collectBundles(logger), systemBundleContext, logger), logger);
	}

	private static void startBundles(List<Bundle> bundles, Logger logger) {
		// Fragment Bundles can't be started so put a check for [Fragment-Host] header.
		bundles.stream().filter(bundle -> bundle.getHeaders().get(HEADER_FRAGMENT_HOST) == null)
				.forEach(bundle -> startBundle(bundle, logger));
	}

	private static void startBundle(Bundle bundle, Logger logger) {
		logger.debug("Starting bundle: [{}], version: [{}]", bundle, bundle.getVersion());
		try {
			bundle.start();
		} catch (BundleException | IllegalStateException | SecurityException ex) {
			logger.error("Exception while installing bundle: [{}]. Exception: {}", bundle, ex);
		}
	}

	private static List<Bundle> installBundles(List<URL> bundles, BundleContext context, Logger logger) throws Exception {
		// First install all the Bundles.
		List<Bundle> installedBundles = bundles.stream().map(url -> {
			return installBundle(context, logger, url);
		}).filter(Objects::nonNull).collect(Collectors.toList());
		logger.info("Total:[{}] Bundles(excluding system bundle) installed!!", installedBundles.size());
		return installedBundles;
	}

	private static Bundle installBundle(BundleContext systemBundleContext, Logger logger, URL url) {
		logger.debug("Installing Bundle from location: [{}]", url);
		Bundle bundle = null;
		try {
			bundle = systemBundleContext.installBundle(url.toExternalForm());
		} catch (BundleException | IllegalStateException | SecurityException ex) {
			logger.error("Exception while installing bundle: [{}]. Exception: {}", url, ex);
		}
		return bundle;
	}

	private static List<URL> collectBundles(Logger logger) throws IOException {
		String rootPath = ServletContextAware.INSTANCE.getServletContext().getInitParameter(BUNDLES_ROOT_DIR_KEY);
		ClassLoader classLoader = BundleProvisioner.class.getClassLoader();
		Predicate<JarEntry> bundlePredicate = (entry) -> entry.getName().startsWith(BUNDLES_JAR_DIR)
				&& entry.getName().endsWith(EXTN_JAR);
		URLConnection conn = BundleProvisioner.class.getResource(rootPath).openConnection();
		List<URL> bundles = JarURLConnection.class.cast(conn).getJarFile().stream().filter(bundlePredicate)
				.map(entry -> classLoader.getResource(entry.getName())).collect(Collectors.toList());
		logger.info("Total:[{}] Bundles(excluding system bundle) collected!!", bundles.size());
		return bundles;
	}
}
