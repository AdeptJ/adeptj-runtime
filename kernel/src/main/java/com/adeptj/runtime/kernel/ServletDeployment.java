package com.adeptj.runtime.kernel;

import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServletDeployment {

    private final SciInfo sciInfo;

    private final List<HttpServlet> servletInstances;

    private final List<ServletInfo> servletInfos;

    private final List<Class<? extends HttpServlet>> servletClasses;

    public ServletDeployment(SciInfo sciInfo) {
        this.sciInfo = sciInfo;
        this.servletInstances = new ArrayList<>();
        this.servletInfos = new ArrayList<>();
        this.servletClasses = new ArrayList<>();
    }

    public SciInfo getSciInfo() {
        return sciInfo;
    }

    public List<HttpServlet> getServletInstances() {
        return servletInstances;
    }

    public void addServletInstances(HttpServlet... servlets) {
        this.servletInstances.addAll(Arrays.asList(servlets));
    }

    public List<Class<? extends HttpServlet>> getServletClasses() {
        return servletClasses;
    }

    public ServletDeployment addServletClass(Class<? extends HttpServlet> servletClass) {
        this.servletClasses.add(servletClass);
        return this;
    }

    public List<ServletInfo> getServletInfos() {
        return servletInfos;
    }

    public ServletDeployment addServletInfo(ServletInfo info) {
        this.servletInfos.add(info);
        return this;
    }

}
