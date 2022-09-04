package com.adeptj.runtime.kernel;

public enum ServerRuntime {

    TOMCAT ("Tomcat"),

    JETTY ("Jetty"),

    UNDERTOW ("Undertow");

    private final String name;

    ServerRuntime(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
