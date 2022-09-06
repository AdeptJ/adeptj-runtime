package com.adeptj.runtime.kernel;

import java.util.List;

public interface Server {

    ServerRuntime getRuntime();

    void start(String[] args, ServletDeployment deployment);

    void postStart();

    void stop();

    void registerServlets(List<ServletInfo> servletInfos);

    void registerFilters(List<FilterInfo> filterInfos);

    void registerErrorPages(List<Integer> errorCodes);

    void addServletContextAttribute(String name, Object value);

    UserManager getUserManager();
}
