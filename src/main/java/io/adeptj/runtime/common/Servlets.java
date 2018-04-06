/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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

package io.adeptj.runtime.common;

import io.adeptj.runtime.osgi.BridgeHttpSessionAttributeListener;
import io.adeptj.runtime.osgi.BridgeHttpSessionIdListener;
import io.adeptj.runtime.osgi.BridgeHttpSessionListener;
import io.adeptj.runtime.osgi.BridgeServletContextAttributeListener;
import io.adeptj.runtime.servlet.BridgeServlet;
import org.apache.commons.lang3.ArrayUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ERROR_PAGE;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME;
import static org.osgi.service.http.whiteboard.HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN;

/**
 * Utility for servlet, filters and listeners
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class Servlets {

    private static final Logger LOGGER = LoggerFactory.getLogger(Servlets.class);

    private static final String BRIDGE_SERVLET = "AdeptJ BridgeServlet";

    private static final String ROOT_MAPPING = "/*";

    private static final String ALL_CONTEXT_SELECT_FILTER = "(osgi.http.whiteboard.context.name=*)";

    private Servlets() {
    }

    public static void registerListener(ServletContext servletContext, EventListener listener) {
        servletContext.addListener(listener);
    }

    public static void registerListeners(ServletContext servletContext, List<EventListener> listeners) {
        listeners.forEach(servletContext::addListener);
    }

    public static void registerBridgeListeners(ServletContext servletContext) {
        servletContext.addListener(new BridgeServletContextAttributeListener());
        servletContext.addListener(new BridgeHttpSessionListener());
        servletContext.addListener(new BridgeHttpSessionIdListener());
        servletContext.addListener(new BridgeHttpSessionAttributeListener());
    }

    public static void registerBridgeServlet(ServletContext context) {
        // Register the BridgeServlet after the OSGi Framework started successfully.
        // This will ensure that the Felix DispatcherServlet is available as an OSGi service and can be tracked.
        // BridgeServlet delegates all the service calls to the Felix DispatcherServlet.
        ServletRegistration.Dynamic bridgeServlet = context.addServlet(BRIDGE_SERVLET, new BridgeServlet());
        bridgeServlet.addMapping(ROOT_MAPPING);
        // Required if [osgi.http.whiteboard.servlet.asyncSupported] is declared true for OSGi HttpService managed Servlets.
        // Otherwise the request processing fails throwing exception [java.lang.IllegalStateException: UT010026:
        // Async is not supported for this request, as not all filters or Servlets were marked as supporting async]
        bridgeServlet.setAsyncSupported(true);
        // Load early to detect any issue with OSGi Felix DispatcherServlet initialization.
        bridgeServlet.setLoadOnStartup(0);
    }

    public static ServiceRegistration<Servlet> osgiServlet(BundleContext ctx, HttpServlet servlet) {
        Class<? extends HttpServlet> cls = servlet.getClass();
        WebServlet webServlet = checkWebServletAnnotation(cls);
        Dictionary<String, Object> properties = new Hashtable<>(); // NOSONAR
        properties.put(HTTP_WHITEBOARD_SERVLET_PATTERN,
                ArrayUtils.isEmpty(webServlet.urlPatterns()) ? webServlet.value() : webServlet.urlPatterns());
        properties.put(HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
        handleInitParams(webServlet, properties);
        handleName(cls, webServlet.name(), properties);
        String servletFQCN = cls.getName();
        LOGGER.info("Registering OSGi Servlet: [{}]", servletFQCN);
        return ctx.registerService(Servlet.class, servlet, properties);
    }

    public static ServiceRegistration<Servlet> osgiServlet(BundleContext ctx, HttpServlet errorServlet, List<String> errors) {
        Class<? extends HttpServlet> cls = errorServlet.getClass();
        WebServlet webServlet = checkWebServletAnnotation(cls);
        Dictionary<String, Object> properties = new Hashtable<>(); // NOSONAR
        properties.put(HTTP_WHITEBOARD_SERVLET_ERROR_PAGE, errors);
        // Apply this ErrorServlet to all the ServletContext instances registered with OSGi.
        properties.put(HTTP_WHITEBOARD_CONTEXT_SELECT, ALL_CONTEXT_SELECT_FILTER);
        properties.put(HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
        handleInitParams(webServlet, properties);
        String servletName = handleName(cls, webServlet.name(), properties);
        LOGGER.info("Registering OSGi ErrorHandler: [{}]", servletName);
        return ctx.registerService(Servlet.class, errorServlet, properties);
    }

    private static WebServlet checkWebServletAnnotation(Class<? extends HttpServlet> cls) {
        return Optional.ofNullable(cls.getAnnotation(WebServlet.class)).orElseThrow(() ->
                new IllegalArgumentException("Can't register a servlet without @WebServlet annotation!!"));
    }

    private static void handleInitParams(WebServlet webServlet, Dictionary<String, Object> properties) {
        Arrays.stream(webServlet.initParams())
                .forEach(initParam ->
                        properties.put(HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX + initParam.name(), initParam.value()));
    }

    private static String handleName(Class<? extends HttpServlet> cls, String name, Dictionary<String, Object> props) {
        String servletName = name.isEmpty() ? cls.getSimpleName() : name;
        props.put(HTTP_WHITEBOARD_SERVLET_NAME, servletName);
        return servletName;
    }
}
