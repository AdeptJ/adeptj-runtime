package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.Server;

/**
 * Bootstrap the given {@link Server} instance.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface ServerBootstrapper {

    /**
     * Bootstrap the given {@link Server} instance.
     *
     * @param server the {@link Server} (Tomcat, Jetty, Undertow) instance.
     * @param args   the program arguments to the server instance.
     */
    void bootstrap(Server server, String[] args);
}
