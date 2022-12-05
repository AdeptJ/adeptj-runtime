package com.adeptj.runtime.htmlrender;

import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Optional;

import static io.quarkus.qute.Variant.TEXT_HTML;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ClasspathTemplateLocation implements TemplateLocator.TemplateLocation {

    private final Locale locale;

    private final Reader reader;

    public ClasspathTemplateLocation(Locale locale, InputStream stream) {
        this.locale = locale;
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    @Override
    public Reader read() {
        return this.reader;
    }

    @Override
    public Optional<Variant> getVariant() {
        return Optional.of(new Variant(this.locale, UTF_8, TEXT_HTML));
    }
}
