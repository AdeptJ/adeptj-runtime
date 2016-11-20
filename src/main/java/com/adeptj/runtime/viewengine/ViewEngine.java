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
package com.adeptj.runtime.viewengine;

import static org.trimou.engine.config.EngineConfigurationKey.END_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.START_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_ENABLED;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_EXPIRATION_TIMEOUT;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.handlebars.Helper;
import org.trimou.handlebars.i18n.ResourceBundleHelper;
import org.trimou.handlebars.i18n.ResourceBundleHelper.Format;

import com.adeptj.runtime.common.TimeUnits;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;

/**
 * ViewEngine.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ViewEngine {

	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngine.class);
	
	private static final String RB_HELPER_NAME = "msg";

	private final MustacheEngine engine;
	
	private TemplateLocator templateLocator(Config config) {
		return new ClassPathTemplateLocator(config.getInt("admin-template-locator-priority"), config.getString("admin-view-root"),
				config.getString("admin-view-suffix"), ViewEngine.class.getClassLoader(), false);
	}

	private Helper resourceBundleHelper(Config config) {
		return new ResourceBundleHelper(config.getString("resource-bundle-basename"), Format.MESSAGE);
	}

	private MustacheEngine mustacheEngine() {
		Config config = Configs.INSTANCE.trimou();
		return MustacheEngineBuilder.newBuilder().registerHelper(RB_HELPER_NAME, this.resourceBundleHelper(config))
				.addTemplateLocator(templateLocator(config))
				.setProperty(START_DELIMITER, config.getString("start-delimiter"))
				.setProperty(END_DELIMITER, config.getString("end-delimiter"))
				.setProperty(TEMPLATE_CACHE_ENABLED, config.getBoolean("template-cache-enabled"))
				.setProperty(TEMPLATE_CACHE_EXPIRATION_TIMEOUT, config.getInt("template-cache-expiration")).build();
	}
	
	ViewEngine() {
		long startTime = System.nanoTime();
		this.engine = this.mustacheEngine();
		LoggerFactory.getLogger(ViewEngine.class).info("MustacheEngine initialized in: [{}] ms!!", TimeUnits.nanosToMillis(startTime));
	}

	public boolean processView(ViewEngineContext context) {
		long startTime = System.nanoTime();
		boolean rendered = false;
		String view = context.getView();
		LOGGER.debug("Processing view: [{}]", view);
		HttpServletResponse response = context.getResponse();
		try {
			Mustache mustache = this.engine.getMustache(view);
			if (mustache == null) {
				LOGGER.info("View: [{}] not found!!", view);
				// Send error so that container's error page mechanism kicks in.
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				response.getWriter().write(mustache.render(context.getModels()));
			    rendered = true;
			    if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processed view: [{}] in: [{}] ms!!", view, TimeUnits.nanosToMillis(startTime));	
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Exception while processing view: [{}]", view, ex);
			this.handleException(context, ex);
		}
		return rendered;
	}

	private void handleException(ViewEngineContext context, Exception ex) {
		context.getRequest().setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);
		try {
			context.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException ioex) {
			// Now what? may be log and re-throw.
			LOGGER.error("Exception while sending error!!", ioex);
			throw new ViewEngineException(ex.getMessage(), ioex);
		}
	}
}
