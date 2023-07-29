package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;

public class UndertowBootstrapper extends AbstractServerBootstrapper {

    @Override
    public void bootstrap(Server server, String[] args) {
        // ServletInfo
        ServletInfo adminServletInfo = this.createAdminServletInfo(false);
        ServletInfo errorServletInfo = this.createErrorServletInfo(false);
        ServletInfo logbackStatusServletInfo = this.createLogbackViewStatusMessagesServlet(false);
        // ServletDeployment
        ServletDeployment deployment = new ServletDeployment(new SciInfo(RuntimeInitializer.class, this.getHandleTypes()));
        deployment.addServletInfos(adminServletInfo, errorServletInfo, logbackStatusServletInfo);
        // Start
        server.start(args, deployment);
    }
}
