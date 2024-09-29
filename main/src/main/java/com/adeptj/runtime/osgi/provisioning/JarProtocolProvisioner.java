package com.adeptj.runtime.osgi.provisioning;

import com.adeptj.runtime.common.OSGiUtil;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

public class JarProtocolProvisioner extends AbstractBundleProvisioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarProtocolProvisioner.class);

    @Override
    AtomicInteger installAndStart(Config felixConf, BundleContext bundleContext) throws IOException {
        AtomicInteger counter = new AtomicInteger(1); // add the system bundle to the total count
        this.collectAsStream(felixConf.getString(BUNDLES_ROOT_DIR_KEY))
                .map(url -> this.install(url, bundleContext, counter))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Bundle::getBundleId)) // start bundles in ascending order of bundle id.
                .filter(OSGiUtil::isNotFragment)
                .forEach(this::start);
        return counter;
    }

    private Stream<URL> collectAsStream(String bundlesDir) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        return this.getJarFile(bundlesDir, cl)
                .stream()
                .filter(jarEntry -> this.isJarEntryFromBundlesDir(jarEntry, bundlesDir))
                .map(jarEntry -> cl.getResource(jarEntry.getName()))
                .filter(Objects::nonNull);
    }

    private Bundle install(URL url, BundleContext bundleContext, AtomicInteger counter) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Installing Bundle from location: [{}]", url);
        }
        Bundle bundle = null;
        try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
            if (OSGiUtil.isNotBundle(jis.getManifest())) {
                LOGGER.error("Artifact [{}] is not a Bundle, skipping install!!", url);
            } else {
                bundle = bundleContext.installBundle(url.toExternalForm());
                counter.getAndIncrement();
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing Bundle: [{}]. Cause:", url, ex);
        }
        return bundle;
    }

    private List<URL> collectAsList(String bundlesDir) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        List<URL> bundles = new ArrayList<>();
        Enumeration<JarEntry> entries = this.getJarFile(bundlesDir, cl).entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (this.isJarEntryFromBundlesDir(jarEntry, bundlesDir)) {
                URL bundle = cl.getResource(jarEntry.getName());
                if (bundle != null) {
                    bundles.add(bundle);
                }
            }
        }
        return bundles;
    }

    private JarFile getJarFile(String bundlesDir, ClassLoader cl) throws IOException {
        URL resource = cl.getResource(bundlesDir);
        if (resource == null) {
            throw new IllegalStateException(String.format("Could not obtain bundles from location [%s]", bundlesDir));
        }
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        return connection.getJarFile();
    }

    private boolean isJarEntryFromBundlesDir(JarEntry jarEntry, String bundlesDir) {
        String name = jarEntry.getName();
        return name.startsWith(bundlesDir) && name.endsWith(DOT_JAR);
    }

    @Override
    boolean handleUpdate(Config felixConf, BundleContext bundleContext) throws IOException {
        List<URL> urls = this.collectAsList(felixConf.getString(BUNDLES_ROOT_DIR_KEY));
        if (urls.isEmpty()) {
            LOGGER.warn("No new bundles to update!!");
            return false;
        }
        boolean restartFramework = false;
        Map<String, Bundle> existingBundles = Stream.of(bundleContext.getBundles())
                .collect(Collectors.toMap(Bundle::getSymbolicName, bundle -> bundle));
        List<Bundle> newBundles = new ArrayList<>();
        for (URL url : urls) {
            restartFramework |= this.doHandleUpdate(url, existingBundles, newBundles, bundleContext);
        }
        // start the newly installed bundles.
        for (Bundle bundle : newBundles) {
            if (OSGiUtil.isNotFragment(bundle)) {
                this.start(bundle);
            }
        }
        return restartFramework;
    }

    private boolean doHandleUpdate(URL url, Map<String, Bundle> existingBundles,
                                   List<Bundle> newBundles, BundleContext bundleContext) {
        boolean restartFramework = false;
        try (JarInputStream jis = new JarInputStream(url.openStream(), false)) {
            Manifest manifest = jis.getManifest();
            if (manifest == null) {
                LOGGER.error("Manifest missing for artifact [{}]", url);
                return false;
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            String symbolicName = mainAttributes.getValue(BUNDLE_SYMBOLICNAME);
            if (StringUtils.isEmpty(symbolicName)) {
                LOGGER.error("Artifact [{}] is not an OSGi Bundle!!", url);
                return false;
            }
            Bundle installedBundle = existingBundles.get(symbolicName);
            if (installedBundle == null) {
                // Install
                Bundle bundle = bundleContext.installBundle(url.toExternalForm());
                newBundles.add(bundle);
                LOGGER.info("Installed new Bundle: [{}, Version: {}]", bundle, bundle.getVersion());
                return false;
            }
            // Update - only when new bundle has higher version than the installed one.
            Version newVersion = Version.parseVersion(mainAttributes.getValue(BUNDLE_VERSION));
            Version installedVersion = installedBundle.getVersion();
            if (newVersion.compareTo(installedVersion) > 0) {
                restartFramework = OSGiUtil.isSystemBundleFragment(installedBundle);
                if (restartFramework) {
                    LOGGER.info("Update for System Bundle fragment, OSGi Framework will be restarted!");
                }
                installedBundle.update(url.openStream());
                LOGGER.info("Updated Bundle: {}, old version was {} and new version is {}",
                        installedBundle, installedVersion, newVersion);
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing/updating Bundle: [{}]. Cause:", url, ex);
        }
        return restartFramework;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }
}
