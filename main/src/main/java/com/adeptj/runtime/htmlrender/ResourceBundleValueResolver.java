package com.adeptj.runtime.htmlrender;

import io.quarkus.qute.CompletedStage;
import io.quarkus.qute.EvalContext;
import io.quarkus.qute.ValueResolver;

import java.util.ResourceBundle;
import java.util.concurrent.CompletionStage;

public class ResourceBundleValueResolver implements ValueResolver {

    public ResourceBundleValueResolver() {
    }

    @Override
    public CompletionStage<Object> resolve(EvalContext context) {
        ResourceBundle rb = (ResourceBundle) context.getBase();
        String key = context.getName().replace(")", "");
        return CompletedStage.of(rb.getString(key));
    }

    @Override
    public boolean appliesTo(EvalContext context) {
        return context.getBase() instanceof ResourceBundle;
    }
}
