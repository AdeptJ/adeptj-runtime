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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

public class ReferenceProtocolProvisioner extends AbstractBundleProvisioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceProtocolProvisioner.class);

    @Override
    AtomicInteger installAndStart(Config felixConf, BundleContext bundleContext) {
        File bundlesDir = new File("bundles");
        File[] jars = bundlesDir.listFiles();
        if (jars == null || jars.length == 0) {
            throw new IllegalStateException("Bundles directory does not exist!!");
        }
        AtomicInteger counter = new AtomicInteger(1); // add the system bundle to the total count
        for (File jar : jars) {
            Bundle bundle = this.install(jar, bundleContext, counter);
            if (bundle != null && OSGiUtil.isNotFragment(bundle)) {
                this.start(bundle);
            }
        }
        return counter;
    }

    private Bundle install(File jar, BundleContext bundleContext, AtomicInteger counter) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Installing Bundle from location: [{}]", jar);
        }
        Bundle bundle = null;
        try (JarFile bundleArchive = new JarFile(jar)) {
            if (OSGiUtil.isNotBundle(bundleArchive.getManifest())) {
                LOGGER.error("Artifact [{}] is not a Bundle, skipping install!!", jar);
            } else {
                bundle = bundleContext.installBundle("reference:file:bundles/" + jar.getName());
                counter.getAndIncrement();
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing Bundle: [{}]. Cause:", jar, ex);
        }
        return bundle;
    }

    @Override
    boolean handleUpdate(Config felixConf, BundleContext bundleContext) {
        File bundlesDir = new File("bundles");
        File[] jars = bundlesDir.listFiles();
        if (jars == null || jars.length == 0) {
            LOGGER.warn("No new bundles to update!!");
            return false;
        }
        boolean restartFramework = false;
        Map<String, Bundle> existingBundles = Stream.of(bundleContext.getBundles())
                .collect(Collectors.toMap(Bundle::getSymbolicName, bundle -> bundle));
        List<Bundle> newBundles = new ArrayList<>();
        for (File jar : jars) {
            restartFramework |= this.doHandleUpdate(jar, existingBundles, newBundles, bundleContext);
        }
        // start the newly installed bundles.
        for (Bundle bundle : newBundles) {
            if (OSGiUtil.isNotFragment(bundle)) {
                this.start(bundle);
            }
        }
        return restartFramework;
    }

    private boolean doHandleUpdate(File jar, Map<String, Bundle> existingBundles,
                                   List<Bundle> newBundles, BundleContext bundleContext) {
        boolean restartFramework = false;
        try (JarFile bundleArchive = new JarFile(jar)) {
            Manifest manifest = bundleArchive.getManifest();
            if (manifest == null) {
                LOGGER.error("Manifest missing for artifact [{}]", jar);
                return false;
            }
            Attributes mainAttributes = manifest.getMainAttributes();
            String symbolicName = mainAttributes.getValue(BUNDLE_SYMBOLICNAME);
            if (StringUtils.isEmpty(symbolicName)) {
                LOGGER.error("Artifact [{}] is not an OSGi Bundle!!", jar);
                return false;
            }
            Bundle installedBundle = existingBundles.get(symbolicName);
            if (installedBundle == null) {
                // Install
                Bundle bundle = bundleContext.installBundle("reference:file:bundles/" + jar.getName());
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
                installedBundle.update(new FileInputStream(jar));
                LOGGER.info("Updated Bundle: {}, old version was {} and new version is {}",
                        installedBundle, installedVersion, newVersion);
            }
        } catch (BundleException | IllegalStateException | SecurityException | IOException ex) {
            LOGGER.error("Exception while installing/updating Bundle: [{}]. Cause:", jar, ex);
        }
        return restartFramework;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }
}
