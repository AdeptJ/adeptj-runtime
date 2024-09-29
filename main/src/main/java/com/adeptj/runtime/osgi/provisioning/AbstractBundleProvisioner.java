package com.adeptj.runtime.osgi.provisioning;

import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBundleProvisioner implements BundleProvisioner {

    @Override
    public boolean installOrUpdateBundles(Config felixConf, BundleContext bundleContext) throws IOException {
        // config directory will not yet be created if framework is being provisioned first time.
        File frameworkConfigDir = Paths.get(felixConf.getString(CFG_KEY_FELIX_CM_DIR)).toFile();
        if (frameworkConfigDir.exists()) {
            if (Boolean.getBoolean(SYS_PROP_FORCE_PROVISION_BUNDLES)) {
                // Update
                return this.handleUpdate(felixConf, bundleContext);
            }
            // Restart, just return.
            return false;
        }
        // Install
        long startTime = System.nanoTime();
        this.getLogger().info("Starting provisioning of bundles on OSGi framework's first bootstrap!!");
        AtomicInteger bundleInstallCount = this.installAndStart(felixConf, bundleContext);
        this.getLogger().info(BUNDLE_PROVISIONED_MSG, bundleInstallCount.get(), Times.elapsedMillis(startTime));
        return false;
    }

    abstract AtomicInteger installAndStart(Config felixConf, BundleContext bundleContext) throws IOException;

    abstract boolean handleUpdate(Config felixConf, BundleContext bundleContext) throws IOException;

    abstract Logger getLogger();

    void start(Bundle bundle) {
        try {
            bundle.start();
            this.getLogger().info("Started Bundle [{}, v{}]", bundle, bundle.getVersion());
        } catch (Exception ex) { // NOSONAR
            this.getLogger().error("Exception while starting Bundle: [{}, Version: {}]", bundle, bundle.getVersion(), ex);
        }
    }
}
