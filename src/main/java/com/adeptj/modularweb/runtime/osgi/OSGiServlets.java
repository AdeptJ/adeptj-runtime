/** 
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
package com.adeptj.modularweb.runtime.osgi;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGiServlets. takes care of OSGi Servlet register and unregister operations.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum OSGiServlets {

	INSTANCE;

	private Map<String, ServiceRegistration<? extends Servlet>> servlets = new HashMap<>();

	public void registerAll(BundleContext ctx, List<HttpServlet> servlets) {
		servlets.forEach(servlet -> this.register(ctx, servlet));
	}

	public void register(BundleContext ctx, HttpServlet httpServlet) {
		Class<? extends HttpServlet> klazz = httpServlet.getClass();
		WebServlet webServlet = klazz.getAnnotation(WebServlet.class);
		if (webServlet == null) {
			throw new IllegalArgumentException("Can't register a servlet without @WebServlet annotation!!");
		}
		String[] urlPatterns = webServlet.urlPatterns();
		if (urlPatterns == null || urlPatterns.length == 0) {
			urlPatterns = webServlet.value();
		}
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, webServlet.name());
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, urlPatterns);
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
		WebInitParam[] initParams = webServlet.initParams();
		for (WebInitParam initParam : initParams) {
			properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX + initParam.name(),
					initParam.value());
		}
		String name = klazz.getName();
		LoggerFactory.getLogger(OSGiServlets.class).info("Registering OSGi Servlet: [{}]", name);
		this.servlets.put(name, ctx.registerService(Servlet.class, httpServlet, properties));
	}
	
	public void registerErrorServlet(BundleContext ctx, HttpServlet errorServlet, List<String> errors) {
		Class<? extends HttpServlet> klazz = errorServlet.getClass();
		WebServlet webServlet = klazz.getAnnotation(WebServlet.class);
		if (webServlet == null) {
			throw new IllegalArgumentException("Can't register a servlet without @WebServlet annotation!!");
		}
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, webServlet.name());
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ERROR_PAGE, errors);
		// Apply this ErrorServlet to all the Servlets registered with "default" contextId.
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "(osgi.http.whiteboard.context.name=*)");
		properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, webServlet.asyncSupported());
		WebInitParam[] initParams = webServlet.initParams();
		for (WebInitParam initParam : initParams) {
			properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_INIT_PARAM_PREFIX + initParam.name(), initParam.value());
		}
		String name = klazz.getName();
		LoggerFactory.getLogger(OSGiServlets.class).info("Registering OSGi ErrorServlet: [{}]", name);
		this.servlets.put(name, ctx.registerService(Servlet.class, errorServlet, properties));
	}

	public void unregister(Class<HttpServlet> klazz) {
		ServiceRegistration<? extends Servlet> serviceRegistration = this.servlets.get(klazz.getName());
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	public void unregisterAll() {
		Logger logger = LoggerFactory.getLogger(OSGiServlets.class);
		this.servlets.forEach((servletName, serviceRegistration) -> {
			logger.info("Unregistering OSGi Servlet: [{}]", servletName);
			serviceRegistration.unregister();
		});
	}
}
