package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.Server;

public interface ServerBootstrapper {

    void bootstrap(Server server, String[] args);
}
