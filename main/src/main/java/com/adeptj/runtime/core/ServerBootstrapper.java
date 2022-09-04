package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.osgi.FrameworkLauncher;

import java.util.LinkedHashSet;
import java.util.Set;

public interface ServerBootstrapper {

    default Set<Class<?>> getHandleTypes() {
        Set<Class<?>> handleTypes = new LinkedHashSet<>();
        handleTypes.add(FrameworkLauncher.class);
        handleTypes.add(DefaultStartupAware.class);
        return handleTypes;
    }

    void bootstrap(Server server, String[] args);
}
