package com.adeptj.runtime.kernel;

import com.typesafe.config.Config;

import java.util.List;

public interface Server {

    ServerRuntime getRuntime();

    /**
     * Starts the given {@link Server} instance.
     *
     * @param deployment Servlet deployment information for (Tomcat or Jetty or Undertow) instance.
     * @param appConfig  the application configuration.
     * @param args       the program arguments to the server instance.
     */
    void start(ServletDeployment deployment, Config appConfig, String[] args) throws Exception;

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
