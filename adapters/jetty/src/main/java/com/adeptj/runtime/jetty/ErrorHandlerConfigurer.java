package com.adeptj.runtime.jetty;

import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.stream.IntStream;

public class ErrorHandlerConfigurer {

    public void configure(ServletContextHandler context) {
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        IntStream.of(401, 403, 404, 500, 503)
                .forEach(value -> errorHandler.addErrorPage(value, "/ErrorHandler"));
        context.setErrorHandler(errorHandler);
    }
}
