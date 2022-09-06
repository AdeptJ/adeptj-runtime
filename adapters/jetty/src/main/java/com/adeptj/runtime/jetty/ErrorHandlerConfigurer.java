package com.adeptj.runtime.jetty;

import com.typesafe.config.Config;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ErrorHandlerConfigurer {

    public void configure(ServletContextHandler context, Config config) {
        Config commonCfg = config.getConfig("main.common");
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        commonCfg.getIntList("error-handler-codes")
                .forEach(value -> errorHandler.addErrorPage(value, "/ErrorHandler"));
        context.setErrorHandler(errorHandler);
    }
}
