package com.adeptj.modularweb.micro.bootstrap;

import static com.adeptj.modularweb.micro.bootstrap.FrameworkConstants.BUNDLES_ROOT_DIR_KEY;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BundleProvisioner that handles the installation/activation of required bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum BundleProvisioner {
	
	INSTANCE;
	
	private static final String BUNDLES_JAR_DIR = "bundles/";
	
	private static final String HEADER_FRAGMENT_HOST = "Fragment-Host";

	private static final String EXTN_JAR = ".jar";

	private static final Logger LOGGER = LoggerFactory.getLogger(BundleProvisioner.class);

	public void provisionBundles(BundleContext systemBundleContext) throws Exception {
		Set<Bundle> installedBundles = this.installBundles(systemBundleContext);
		LOGGER.info("Total:[{}] Bundles(excluding system bundle) installed!!", installedBundles.size());
		// Now start all the installed Bundles.
		this.startBundles(installedBundles);
	}

	private void startBundles(Set<Bundle> installedBundles) {
		// Fragment Bundles can't be started so put a check for [Fragment-Host] header.
		installedBundles.stream().filter(bundle -> bundle.getHeaders().get(HEADER_FRAGMENT_HOST) == null)
				.forEach(bundle -> {
					LOGGER.info("Starting bundle: [{}] version: [{}]", bundle, bundle.getVersion());
					try {
						bundle.start();
					} catch (BundleException | IllegalStateException | SecurityException  ex) {
						LOGGER.error("Exception while starting bundle: [{}]. Exception: {}", bundle, ex);
					}
				});
	}

	private Set<Bundle> installBundles(BundleContext systemBundleContext) throws Exception {
		Set<Bundle> installedBundles = new HashSet<>();
		// First install all the Bundles.
		this.collectBundles().forEach(url -> {
			LOGGER.debug("Installing Bundle from location: [{}]", url);
			try {
				installedBundles.add(systemBundleContext.installBundle(url.toExternalForm()));
			} catch (BundleException | IllegalStateException | SecurityException ex) {
				LOGGER.error("Exception while installing bundle: [{}]. Exception: {}", url, ex);
			}
		});
		return installedBundles;
	}

	private List<URL> collectBundles() throws IOException {
		String rootPath = ServletContextAware.INSTANCE.getServletContext().getInitParameter(BUNDLES_ROOT_DIR_KEY);
		JarURLConnection conn = (JarURLConnection) BundleProvisioner.class.getResource(rootPath).openConnection();
		ClassLoader classLoader = BundleProvisioner.class.getClassLoader();
		List<URL> bundles = conn.getJarFile().stream()
				.filter(entry -> entry.getName().startsWith(BUNDLES_JAR_DIR) && entry.getName().endsWith(EXTN_JAR))
				.map(entry -> classLoader.getResource(entry.getName())).collect(Collectors.toList());
		LOGGER.info("Total:[{}] Bundles(excluding system bundle) collected from location:[{}]", bundles.size(), conn);
		return bundles;
	}
}
