/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.osgi;

import org.apache.commons.lang3.ArrayUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ERROR_PAGE;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN;

/**
 * OSGiServlets. takes care of OSGi Servlet register and unregister operations.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum OSGiServlets {

    INSTANCE;

    private static final String ALL_CONTEXT_SELECT_FILTER = "(osgi.http.whiteboard.context.name=*)";

    /**
     * HttpServlet FQCN to ServiceRegistration mapping.
     */
    private Map<String, ServiceRegistration<? extends Servlet>> servlets = new HashMap<>();

    public void registerAll(BundleContext ctx, List<HttpServlet> servlets) {
        servlets.forEach(servlet -> this.register(ctx, servlet));
    }

    public void register(BundleContext ctx, HttpServlet servlet) {
        Class<? extends HttpServlet> cls = servlet.getClass();
        WebServlet webServlet = this.checkWebServletAnnotation(cls);
        Dictionary<String, Object> properties = new Hashtable<>(); // NOSONAR
        properties.put(HTTP_WHITEBOARD_SERVLET_PATTERN,
                ArrayUtils.isEmpty(webServlet.urlPatterns()) ? webServlet.value() : webServlet.urlPatterns());
        properties.put(HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
        this.handleInitParams(webServlet, properties);
        this.handleName(cls, webServlet.name(), properties);
        String servletFQCN = cls.getName();
        LoggerFactory.getLogger(OSGiServlets.class).info("Registering OSGi Servlet: [{}]", servletFQCN);
        this.servlets.put(servletFQCN, ctx.registerService(Servlet.class, servlet, properties));
    }

    protected void registerErrorServlet(BundleContext ctx, HttpServlet errorServlet, List<String> errors) {
        Class<? extends HttpServlet> cls = errorServlet.getClass();
        WebServlet webServlet = this.checkWebServletAnnotation(cls);
        Dictionary<String, Object> properties = new Hashtable<>(); // NOSONAR
        properties.put(HTTP_WHITEBOARD_SERVLET_ERROR_PAGE, errors);
        // Apply this ErrorServlet to all the ServletContext instances registered with OSGi.
        properties.put(HTTP_WHITEBOARD_CONTEXT_SELECT, ALL_CONTEXT_SELECT_FILTER);
        properties.put(HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
        this.handleInitParams(webServlet, properties);
        this.handleName(cls, webServlet.name(), properties);
        String servletFQCN = cls.getName();
        LoggerFactory.getLogger(OSGiServlets.class).info("Registering OSGi ErrorServlet: [{}]", servletFQCN);
        this.servlets.put(servletFQCN, ctx.registerService(Servlet.class, errorServlet, properties));
    }

    public void unregister(Class<HttpServlet> cls) {
        Optional.ofNullable(this.servlets.get(cls.getName())).ifPresent(ServiceRegistration::unregister);
    }

    public void unregisterAll() {
        Logger logger = LoggerFactory.getLogger(OSGiServlets.class);
        this.servlets.forEach((servletName, serviceRegistration) -> {
            logger.info("Unregistering OSGi Servlet: [{}]", servletName);
            serviceRegistration.unregister();
        });
    }

    private WebServlet checkWebServletAnnotation(Class<? extends HttpServlet> cls) {
        return Optional.ofNullable(cls.getAnnotation(WebServlet.class)).orElseThrow(() ->
                new IllegalArgumentException("Can't register a servlet without @WebServlet annotation!!"));
    }

    private void handleInitParams(WebServlet webServlet, Dictionary<String, Object> properties) {
        Arrays.stream(webServlet.initParams())
                .forEach(initParam -> properties.put(HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX + initParam.name(),
                        initParam.value()));
    }

    private void handleName(Class<? extends HttpServlet> cls, String name, Dictionary<String, Object> props) {
        props.put(HTTP_WHITEBOARD_SERVLET_NAME, name.isEmpty() ? cls.getSimpleName() : name);
    }
}
