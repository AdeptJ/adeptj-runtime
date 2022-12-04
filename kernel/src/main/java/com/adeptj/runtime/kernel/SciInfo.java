package com.adeptj.runtime.kernel;

import jakarta.servlet.ServletContainerInitializer;
import java.util.Set;

public class SciInfo {

    private ServletContainerInitializer sci;

    private final Set<Class<?>> handleTypes;

    private Class<? extends ServletContainerInitializer> sciClass;

    // Tomcat and Jetty expects an instance of ServletContainerInitializer.
    public SciInfo(ServletContainerInitializer sci, Set<Class<?>> handleTypes) {
        this.sci = sci;
        this.handleTypes = handleTypes;
    }

    // Undertow expects ServletContainerInitializer class.
    public SciInfo(Class<? extends ServletContainerInitializer> sciClass, Set<Class<?>> handleTypes) {
        this.sciClass = sciClass;
        this.handleTypes = handleTypes;
    }

    public ServletContainerInitializer getSciInstance() {
        return sci;
    }

    public Class<? extends ServletContainerInitializer> getSciClass() {
        return sciClass;
    }

    public Set<Class<?>> getHandleTypes() {
        return handleTypes;
    }

    public Class<?>[] getHandleTypesArray() {
        return this.handleTypes.toArray(new Class[0]);
    }
}
