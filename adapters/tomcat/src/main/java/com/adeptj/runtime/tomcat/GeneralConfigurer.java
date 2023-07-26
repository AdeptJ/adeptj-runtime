package com.adeptj.runtime.tomcat;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.util.Map;

public class GeneralConfigurer {

    public void configure(StandardContext context, Config serverConfig) {
        // Filters
        this.configFilters(context, serverConfig);
        // Servlets
        this.configServlets(context, serverConfig);
        // Error Pages
        this.configErrorPages(context, serverConfig);
    }

    private void configFilters(StandardContext context, Config serverConfig) {
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

    private void configServlets(StandardContext context, Config serverConfig) {
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

    private void configErrorPages(StandardContext context, Config serverConfig) {
        for (Integer errorCode : serverConfig.getIntList("error-codes")) {
            ErrorPage errorPage = new ErrorPage();
            errorPage.setErrorCode(errorCode);
            errorPage.setLocation(serverConfig.getString("error-handler-path"));
            context.addErrorPage(errorPage);
        }
    }
}
