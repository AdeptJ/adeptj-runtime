/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
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

import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_ASYNC;

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
            def.setAsyncSupported(config.getString(CFG_KEY_ASYNC));
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
            if (config.hasPath(CFG_KEY_ASYNC)) {
                servlet.setAsyncSupported(config.getBoolean(CFG_KEY_ASYNC));
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
