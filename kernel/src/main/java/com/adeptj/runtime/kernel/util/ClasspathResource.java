package com.adeptj.runtime.kernel.util;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class ClasspathResource {

    public static URL toUrl(String resourceName, ClassLoader cl) {
        return cl.getResource(resourceName);
    }

    public static Enumeration<URL> toUrls(String resourceName, ClassLoader cl) throws IOException {
        return cl.getResources(resourceName);
    }
}
