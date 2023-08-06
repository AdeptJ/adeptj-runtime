package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.typesafe.config.Config;

public class TomcatBootstrapper extends AbstractServerBootstrapper {

    @Override
    public void bootstrap(Server server, Config appConfig, String[] args) throws Exception {
        // ServletDeployment
        ServletDeployment deployment = new ServletDeployment(new SciInfo(new RuntimeInitializer(), this.getHandleTypes()));
        deployment.addServletInfos(this.getServlets());
        // Start
        server.start(deployment, appConfig, args);
    }
}
