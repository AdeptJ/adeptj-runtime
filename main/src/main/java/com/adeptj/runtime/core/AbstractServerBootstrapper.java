package com.adeptj.runtime.core;

import ch.qos.logback.classic.ViewStatusMessagesServlet;
import com.adeptj.runtime.kernel.ServletInfo;
import com.adeptj.runtime.osgi.FrameworkLauncher;
import com.adeptj.runtime.servlet.AdminServlet;
import com.adeptj.runtime.servlet.ErrorServlet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.LOGBACK_VIEW_SERVLET_NAME;
import static com.adeptj.runtime.common.Constants.LOGBACK_VIEW_SERVLET_URI;

public abstract class AbstractServerBootstrapper implements ServerBootstrapper {

    protected Set<Class<?>> getHandleTypes() {
        Set<Class<?>> handleTypes = new LinkedHashSet<>();
        handleTypes.add(FrameworkLauncher.class);
        handleTypes.add(DefaultStartupAware.class);
        return handleTypes;
    }

    protected List<ServletInfo> getServlets() {
        List<ServletInfo> servletInfos = new ArrayList<>();
        servletInfos.add(new ServletInfo(ADMIN_SERVLET_NAME, ADMIN_SERVLET_URI, AdminServlet.class));
        servletInfos.add(new ServletInfo(ERROR_SERVLET_NAME, ERROR_SERVLET_URI, ErrorServlet.class));
        servletInfos.add(new ServletInfo(LOGBACK_VIEW_SERVLET_NAME, LOGBACK_VIEW_SERVLET_URI, ViewStatusMessagesServlet.class));
        return servletInfos;
    }
}
