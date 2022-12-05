package com.adeptj.runtime.htmlrender;

import io.quarkus.qute.TemplateLocator;

import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

public class ClasspathTemplateLocator implements TemplateLocator {

    @Override
    public Optional<TemplateLocation> locate(String id) {
        try {
            InputStream stream = this.getClass().getResourceAsStream("/webapp/templates/" + id + ".html");
            if (stream == null) {
                return Optional.empty();
            }
            return Optional.of(new ClasspathTemplateLocation(Locale.getDefault(), stream));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
