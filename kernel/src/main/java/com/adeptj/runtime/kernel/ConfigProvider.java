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

    public Config getApplicationConfig() {
        return this.weakReference.get();
    }

    public Config getMainConfig() {
        return this.getApplicationConfig().getConfig("main");
    }

    public Config getKernelConfig() {
        return this.getApplicationConfig().getConfig("kernel");
    }

    public Config getServerConfig(ServerRuntime runtime) {
        Config applicationConfig = this.getApplicationConfig();
        return applicationConfig.getConfig(runtime.getName().toLowerCase());
    }

    public static ConfigProvider getInstance() {
        return INSTANCE;
    }

}
