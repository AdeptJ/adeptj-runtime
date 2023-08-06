package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.Server;
import com.typesafe.config.Config;

/**
 * Bootstrap the given {@link Server} instance.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface ServerBootstrapper {

    /**
     * Bootstrap the given {@link Server} instance.
     *
     * @param server    the {@link Server} (Tomcat, Jetty, Undertow) instance.
     * @param appConfig the application configuration.
     * @param args      the program arguments to the server instance.
     */
    void bootstrap(Server server, Config appConfig, String[] args) throws Exception;
}
