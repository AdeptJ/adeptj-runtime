package com.adeptj.runtime.kernel;

import java.util.ArrayList;
import java.util.Collections;
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

    public void addServletInfos(List<ServletInfo> infos) {
        this.servletInfos.addAll(infos);
    }

    public List<FilterInfo> getFilterInfos() {
        return filterInfos;
    }

    public void addFilterInfos(FilterInfo... infos) {
        Collections.addAll(this.filterInfos, infos);
    }
}
