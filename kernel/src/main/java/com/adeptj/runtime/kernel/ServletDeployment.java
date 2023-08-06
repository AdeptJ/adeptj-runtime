package com.adeptj.runtime.kernel;

import java.util.ArrayList;
import java.util.List;

public class ServletDeployment {

    private final SciInfo sciInfo;

    // Servlet
    private final List<ServletInfo> servletInfos;

    // Filter
    private final List<FilterInfo> filterInfos;

    public ServletDeployment(SciInfo sciInfo) {
        this.sciInfo = sciInfo;
        this.servletInfos = new ArrayList<>();
        this.filterInfos = new ArrayList<>();
    }

    public SciInfo getSciInfo() {
        return sciInfo;
    }

    public List<ServletInfo> getServletInfos() {
        return servletInfos;
    }

    public ServletDeployment addServletInfo(ServletInfo info) {
        this.servletInfos.add(info);
        return this;
    }

    public List<FilterInfo> getFilterInfos() {
        return filterInfos;
    }

    public ServletDeployment addFilterInfo(FilterInfo info) {
        this.filterInfos.add(info);
        return this;
    }
}
