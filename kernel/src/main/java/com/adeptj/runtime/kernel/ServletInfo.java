package com.adeptj.runtime.kernel;

import javax.servlet.http.HttpServlet;

public class ServletInfo {

    private final String servletName;

    private final String path;

    private Class<? extends HttpServlet> servletClass;

    private HttpServlet servletInstance;

    public ServletInfo(String servletName, String path) {
        this.servletName = servletName;
        this.path = path;
    }

    public String getServletName() {
        return servletName;
    }

    public String getPath() {
        return path;
    }

    public Class<? extends HttpServlet> getServletClass() {
        return servletClass;
    }

    public void setServletClass(Class<? extends HttpServlet> servletClass) {
        this.servletClass = servletClass;
    }

    public HttpServlet getServletInstance() {
        return servletInstance;
    }

    public void setServletInstance(HttpServlet servletInstance) {
        this.servletInstance = servletInstance;
    }
}
