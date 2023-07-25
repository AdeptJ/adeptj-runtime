package com.adeptj.runtime.kernel;

public enum ServerRuntime {

    JETTY("Jetty"),

    TOMCAT("Tomcat"),

    UNDERTOW("Undertow");

    private final String name;

    ServerRuntime(String name) {
        this.name = name;
    }

    public String getLowerCaseName() {
        return name.toLowerCase();
    }

    @Override
    public String toString() {
        return name;
    }
}
