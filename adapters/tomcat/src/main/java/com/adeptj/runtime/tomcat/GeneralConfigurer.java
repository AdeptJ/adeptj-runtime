package com.adeptj.runtime.tomcat;

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.util.stream.IntStream;

public class GeneralConfigurer {

    public void configure(StandardContext context) {
        // ContextPathFilter
        FilterDef def = new FilterDef();
        def.setAsyncSupported("true");
        def.setFilterName("ContextPathFilter");
        def.setDisplayName("Filter for handling context path(/) requests");
        def.setFilter(new ContextPathFilter());
        context.addFilterDef(def);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("ContextPathFilter");
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
        // ResourceServlet
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass(ResourceServlet.class.getName());
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.addInitParameter("fileEncoding", "UTF-8");
        defaultServlet.setLoadOnStartup(1);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/static/*", "default");
        // Error Pages
        IntStream.of(401, 403, 404, 500, 503)
                .forEach(value -> {
                    ErrorPage errorPage = new ErrorPage();
                    errorPage.setErrorCode(value);
                    errorPage.setLocation("/ErrorHandler");
                    context.addErrorPage(errorPage);
                });
    }
}
