package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.typesafe.config.Config;

public class TomcatBootstrapper extends AbstractServerBootstrapper {

    @Override
    public void bootstrap(Server server, Config appConfig, String[] args) throws Exception {
        // ServletInfo
        ServletInfo adminServletInfo = this.createAdminServletInfo(true);
        ServletInfo errorServletInfo = this.createErrorServletInfo(true);
        ServletInfo logbackStatusServletInfo = this.createLogbackViewStatusMessagesServlet(true);
        // ServletDeployment
        ServletDeployment deployment = new ServletDeployment(new SciInfo(new RuntimeInitializer(), this.getHandleTypes()));
        deployment.addServletInfos(adminServletInfo, errorServletInfo, logbackStatusServletInfo);
        // Start
        server.start(deployment, appConfig, args);
    }
}
