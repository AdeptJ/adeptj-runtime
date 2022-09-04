package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;

public class TomcatBootstrapper extends AbstractServerBootstrapper {

    @Override
    public void bootstrap(Server server, String[] args) {
        // ServletInfo
        ServletInfo adminServletInfo = this.createAdminServletInfo(true);
        ServletInfo errorServletInfo = this.createErrorServletInfo(true);
        // ServletDeployment
        ServletDeployment deployment = new ServletDeployment(new SciInfo(new RuntimeInitializer(), this.getHandleTypes()));
        deployment.addServletInfos(adminServletInfo, errorServletInfo);
        // Start
        server.start(args, deployment);
    }
}
