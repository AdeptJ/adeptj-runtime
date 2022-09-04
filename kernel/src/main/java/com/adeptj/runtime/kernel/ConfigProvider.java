package com.adeptj.runtime.kernel;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.lang.ref.WeakReference;

public enum ConfigProvider {

    INSTANCE;

    private final WeakReference<Config> weakReference;

    ConfigProvider() {
        this.weakReference = new WeakReference<>(ConfigFactory.load());
    }

    public Config getReferenceConfig() {
        return this.weakReference.get();
    }

    public Config getServerConfig(ServerRuntime runtime) {
        return this.getReferenceConfig().getConfig(runtime.getName().toLowerCase());
    }

    public static ConfigProvider getInstance() {
        return INSTANCE;
    }

}
