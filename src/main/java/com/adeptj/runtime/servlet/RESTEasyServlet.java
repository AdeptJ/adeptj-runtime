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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.OSGiUtils;
import com.adeptj.runtime.exception.InitializationException;
import com.adeptj.runtime.jaxrs.GeneralValidatorContextResolver;
import com.adeptj.runtime.jaxrs.ResourceTracker;
import com.adeptj.runtime.osgi.ServiceTrackers;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.validation.GeneralValidator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField;

/**
 * RESTEasyServlet extends RESTEasy HttpServletDispatcher so that the providers can be loaded from 
 * META-INF/services using Framework class loader.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@WebServlet(name = "RESTEasy HttpServletDispatcher", urlPatterns = "/*", asyncSupported = true, initParams = {
		@WebInitParam(name = "resteasy.servlet.mapping.prefix", value = "/") })
public class RESTEasyServlet extends HttpServletDispatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(RESTEasyServlet.class);

	private static final long serialVersionUID = 8759503561853047365L;
	
	private static final String JAXRS_RESOURCE_SERVICE_FILTER = "(&(objectClass=*)(osgi.jaxrs.resource.base=*))";

	private static final String FIELD_CTX_RESOLVERS = "contextResolvers";

    private static final String FIELD_PROVIDER_CLASSES = "providerClasses";

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		try {
			super.init(servletConfig);
			Dispatcher dispatcher = this.getDispatcher();
			BundleContext context = BundleContextHolder.INSTANCE.getBundleContext();
			ServiceTrackers.INSTANCE.track(ResourceTracker.class, new ResourceTracker(context, OSGiUtils.anyServiceFilter(
			        context, JAXRS_RESOURCE_SERVICE_FILTER), dispatcher.getRegistry()));
			this.registerGeneralValidatorContextResolver(dispatcher.getProviderFactory());
			LOGGER.info("RESTEasy HttpServletDispatcher initialized successfully!!");
		} catch (Exception ex) { // NOSONAR
			LOGGER.error("Exception while initializing RESTEasy HttpServletDispatcher!!", ex);
			throw new InitializationException(ex.getMessage(), ex);
		}
	}

	protected void registerGeneralValidatorContextResolver(ResteasyProviderFactory factory) {
		try {
			Map.class.cast(getDeclaredField(ResteasyProviderFactory.class, FIELD_CTX_RESOLVERS , true).get(factory))
					.remove(GeneralValidator.class);
			Set.class.cast(getDeclaredField(ResteasyProviderFactory.class, FIELD_PROVIDER_CLASSES, true).get(factory))
					.remove(GeneralValidatorContextResolver.class);
			factory.registerProvider(GeneralValidatorContextResolver.class);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			LOGGER.error("Exception while adding ContextResolver", ex);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		ServiceTrackers.INSTANCE.close(ResourceTracker.class);
	}
}
