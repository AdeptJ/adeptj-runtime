package io.adeptj.runtime.osgi;

import io.adeptj.runtime.common.OSGiUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.engine.MustacheEngine;

public class MustacheEngineTracker extends ServiceTracker<MustacheEngine, MustacheEngine> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MustacheEngineTracker.class);

    private MustacheEngine mustacheEngine;

    MustacheEngineTracker(BundleContext context) {
        super(context, MustacheEngine.class, null);
    }

    @Override
    public MustacheEngine addingService(ServiceReference<MustacheEngine> reference) {
        LOGGER.info("Adding OSGi service [{}]", OSGiUtil.getServiceDesc(reference));
        this.mustacheEngine = super.addingService(reference);
        return this.mustacheEngine;
    }

    @Override
    public void modifiedService(ServiceReference<MustacheEngine> reference, MustacheEngine service) {
        super.modifiedService(reference, service);
    }

    @Override
    public void removedService(ServiceReference<MustacheEngine> reference, MustacheEngine service) {
        super.removedService(reference, service);
        this.mustacheEngine = null;
    }

    MustacheEngine getMustacheEngine() {
        return mustacheEngine;
    }
}
