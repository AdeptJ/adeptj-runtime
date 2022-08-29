package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.Server;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.servlet.AdminServlet;
import com.adeptj.runtime.servlet.ErrorServlet;

import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;

public class UndertowBootstrapper implements ServerBootstrapper {

    @Override
    public void bootstrap(Server server, String[] args) {
        // ServletInfo
        ServletInfo adminServletInfo = new ServletInfo(ADMIN_SERVLET_NAME, ADMIN_SERVLET_URI);
        adminServletInfo.setServletClass(AdminServlet.class);
        ServletInfo errorServletInfo = new ServletInfo(ERROR_SERVLET_NAME, ERROR_SERVLET_URI);
        errorServletInfo.setServletClass(ErrorServlet.class);

        // ServletDeployment
        ServletDeployment deployment = new ServletDeployment(new SciInfo(RuntimeInitializer.class, this.getHandleTypes()));
        deployment.addServletInfo(adminServletInfo).addServletInfo(errorServletInfo);

        // Start
        server.start(args, deployment);
    }
}
