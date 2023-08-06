package com.adeptj.runtime.tomcat;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GeneralConfigurer {

    public void configure(Context context, Config commonConfig, Config serverConfig) {
        // Filters
        this.configureFilters(context, serverConfig);
        // Servlets
        this.configureServlets(context, serverConfig);
        // Error Pages
        this.configureErrorPages(context, commonConfig);
        int sessionMaxAge = (int) TimeUnit.SECONDS.toMinutes(commonConfig.getInt("session-timeout"));
        context.setSessionTimeout(sessionMaxAge);
    }

    private void configureFilters(Context context, Config serverConfig) {
        for (Config config : serverConfig.getConfigList("filters")) {
            FilterDef def = new FilterDef();
            def.setAsyncSupported(config.getString("async"));
            def.setFilterName(config.getString("name"));
            def.setFilterClass(config.getString("class"));
            for (Map.Entry<String, ConfigValue> entry : config.getConfig("init-params").entrySet()) {
                def.addInitParameter(entry.getKey(), (String) entry.getValue().unwrapped());
            }
            context.addFilterDef(def);
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(config.getString("name"));
            filterMap.addURLPattern(config.getString("pattern"));
            context.addFilterMap(filterMap);
        }
    }

    private void configureServlets(Context context, Config serverConfig) {
        for (Config config : serverConfig.getConfigList("servlets")) {
            Wrapper servlet = context.createWrapper();
            servlet.setName(config.getString("name"));
            servlet.setServletClass(config.getString("class"));
            if (config.hasPath("load-on-startup")) {
                servlet.setLoadOnStartup(config.getInt("load-on-startup"));
            }
            if (config.hasPath("async")) {
                servlet.setAsyncSupported(config.getBoolean("async"));
            }
            for (Map.Entry<String, ConfigValue> entry : config.getConfig("init-params").entrySet()) {
                servlet.addInitParameter(entry.getKey(), (String) entry.getValue().unwrapped());
            }
            context.addChild(servlet);
            context.addServletMappingDecoded(config.getString("pattern"), config.getString("name"));
        }
    }

    private void configureErrorPages(Context context, Config commonConfig) {
        String errorHandlerPath = commonConfig.getString("error-handler-path");
        for (Integer errorCode : commonConfig.getIntList("error-handler-codes")) {
            ErrorPage errorPage = new ErrorPage();
            errorPage.setErrorCode(errorCode);
            errorPage.setLocation(errorHandlerPath);
            context.addErrorPage(errorPage);
        }
    }
}
