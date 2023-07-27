package com.adeptj.runtime.jetty;

import com.typesafe.config.Config;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ErrorHandlerConfigurer {

    public void configure(ServletContextHandler context, Config appConfig) {
        Config commonConfig = appConfig.getConfig("main.common");
        String errorHandlerPath = commonConfig.getString("error-handler-path");
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        commonConfig.getIntList("error-handler-codes")
                .forEach(value -> errorHandler.addErrorPage(value, errorHandlerPath));
        context.setErrorHandler(errorHandler);
    }
}
