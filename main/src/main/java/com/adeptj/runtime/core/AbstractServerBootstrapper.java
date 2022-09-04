package com.adeptj.runtime.core;

import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.servlet.AdminServlet;
import com.adeptj.runtime.servlet.ErrorServlet;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;

public abstract class AbstractServerBootstrapper implements ServerBootstrapper {

    protected Set<Class<?>> getHandleTypes() {
        Set<Class<?>> handleTypes = new LinkedHashSet<>();
        handleTypes.add(FrameworkLauncher.class);
        handleTypes.add(DefaultStartupAware.class);
        return handleTypes;
    }

    protected ServletInfo createAdminServletInfo(boolean createServletInstance) {
        ServletInfo adminServletInfo = new ServletInfo(ADMIN_SERVLET_NAME, ADMIN_SERVLET_URI);
        if (createServletInstance) {
            adminServletInfo.setServletInstance(new AdminServlet());
        } else {
            adminServletInfo.setServletClass(AdminServlet.class);
        }
        return adminServletInfo;
    }

    protected ServletInfo createErrorServletInfo(boolean createServletInstance) {
        ServletInfo errorServletInfo = new ServletInfo(ERROR_SERVLET_NAME, ERROR_SERVLET_URI);
        if (createServletInstance) {
            errorServletInfo.setServletInstance(new ErrorServlet());
        } else {
            errorServletInfo.setServletClass(ErrorServlet.class);
        }
        return errorServletInfo;
    }
}
