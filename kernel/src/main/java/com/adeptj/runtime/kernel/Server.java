package com.adeptj.runtime.kernel;

import java.util.List;

public interface Server {

    ServerRuntime getRuntime();

    void start(String[] args, ServletDeployment deployment);

    default void postStart() {
        // NOOP
    }

    void stop();

    default void registerServlets(List<ServletInfo> servletInfos) {
    }

    default void registerFilters(List<FilterInfo> filterInfos) {
    }

    default void registerErrorPages(List<Integer> errorCodes) {
    }

    void addServletContextAttribute(String name, Object value);

    UserManager getUserManager();
}
