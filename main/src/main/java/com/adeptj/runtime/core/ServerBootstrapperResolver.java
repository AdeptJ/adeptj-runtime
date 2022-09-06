package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.ServerRuntime;

import static com.adeptj.runtime.kernel.ServerRuntime.JETTY;
import static com.adeptj.runtime.kernel.ServerRuntime.TOMCAT;
import static com.adeptj.runtime.kernel.ServerRuntime.UNDERTOW;

public class ServerBootstrapperResolver {

    public static ServerBootstrapper resolve(ServerRuntime runtime) {
        ServerBootstrapper bootstrapper;
        if (runtime == TOMCAT) {
            bootstrapper = new TomcatBootstrapper();
        } else if (runtime == JETTY) {
            bootstrapper = new JettyBootstrapper();
        } else if (runtime == UNDERTOW) {
            bootstrapper = new UndertowBootstrapper();
        } else {
            throw new IllegalStateException("Unknown ServerRuntime: " + runtime);
        }
        return bootstrapper;
    }
}
