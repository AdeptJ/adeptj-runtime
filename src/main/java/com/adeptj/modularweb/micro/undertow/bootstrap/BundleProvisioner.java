package com.adeptj.modularweb.micro.undertow.bootstrap;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BundleProvisioner that handles the installation/activation of required
 * bundles after the system bundle is up and running.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum BundleProvisioner {
	
	INSTANCE;
	
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
		installedBundles.stream().filter((bundle) -> bundle.getHeaders().get(HEADER_FRAGMENT_HOST) == null)
				.forEach((bundle) -> {
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
		this.collectBundles().forEach((url) -> {
			LOGGER.debug("Installing Bundle from location: [{}]", url);
			try {
				installedBundles.add(systemBundleContext.installBundle(url.toExternalForm()));
			} catch (BundleException | IllegalStateException | SecurityException  ex) {
				LOGGER.error("Exception while installing bundle: [{}]. Exception: {}", url, ex);
			}
		});
		return installedBundles;
	}

	private List<URL> collectBundles() throws URISyntaxException, IOException {
		ServletContext servletContext = ServletContextAware.INSTANCE.getServletContext();
		String rootPath = servletContext.getInitParameter(FrameworkConstants.BUNDLES_ROOT_DIR_KEY);
		List<URL> bundles = new ArrayList<>();
		URLConnection conn = BundleProvisioner.class.getResource(rootPath).openConnection();
		if (conn instanceof JarURLConnection) {
			JarFile jar = ((JarURLConnection) conn).getJarFile();
			Enumeration<JarEntry> entries = jar.entries();
			ClassLoader classLoader = BundleProvisioner.class.getClassLoader();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith("bundles/") && name.endsWith(EXTN_JAR)) {
					LOGGER.debug("Adding Bundle: [{}]", name);
					bundles.add(classLoader.getResource(name));
				}
			}
		}
		LOGGER.info("Total:[{}] Bundles(excluding system bundle) collected from location:[{}]", bundles.size(),
				rootPath);
		return bundles;
	}
}
