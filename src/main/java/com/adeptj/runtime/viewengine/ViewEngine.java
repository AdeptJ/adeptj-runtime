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

	private Helper resourceBundleHelper() {
		return new ResourceBundleHelper("messages", Format.MESSAGE);
	}

	private MustacheEngine mustacheEngine() {
		Config config = Configs.INSTANCE.common();
		return MustacheEngineBuilder.newBuilder().registerHelper(RB_HELPER_NAME, this.resourceBundleHelper())
				.addTemplateLocator(templateLocator(config)).build();
	}
	
	ViewEngine() {
		long startTime = System.nanoTime();
		this.engine = this.mustacheEngine();
		LoggerFactory.getLogger(ViewEngine.class).info("MustacheEngine initialized in: [{}] ms!!", TimeUnits.nanosToMillis(startTime));
	}

	public boolean processView(ViewEngineContext context) throws ViewEngineException {
		long startTime = System.nanoTime();
		boolean rendered = false;
		String view = context.getView();
		LOGGER.debug("Processing view: [{}]", view);
		try {
			Mustache mustache = this.engine.getMustache(view);
			if (mustache == null) {
				LOGGER.info("View: [{}] not found!!", view);
			} else {
				context.getResponse().getWriter().write(mustache.render(context.getModels()));
				rendered = true;
			}
		} catch (Exception ex) {
			LOGGER.error("Exception while processing view: [{}]", view, ex);
			throw new ViewEngineException(ex.getMessage(), ex);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processed view: [{}] in: [{}] ms!!", view, TimeUnits.nanosToMillis(startTime));	
		}
		return rendered;
	}
}
