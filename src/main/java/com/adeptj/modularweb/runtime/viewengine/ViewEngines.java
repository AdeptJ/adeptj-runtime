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
package com.adeptj.modularweb.runtime.viewengine;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.adeptj.modularweb.runtime.config.Configs;
import com.typesafe.config.Config;

/**
 * ViewEngines.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ViewEngines {

	THYMELEAF {

		private TemplateEngine initTemplateEngine() {
			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			Config config = Configs.INSTANCE.common();
			templateResolver.setPrefix(config.getString("thymeleaf-template-prefix"));
			templateResolver.setSuffix(config.getString("thymeleaf-template-suffix"));
			templateResolver.setCharacterEncoding(config.getString("thymeleaf-template-encoding"));
			templateResolver.setTemplateMode(config.getString("thymeleaf-template-mode"));
			templateResolver.setCacheable(config.getBoolean("thymeleaf-template-cacheable"));
			// Template cache TTL=1h
			templateResolver.setCacheTTLMs(config.getLong("thymeleaf-template-cacheTTLMs"));
			templateResolver.setOrder(config.getInt("thymeleaf-templateresolver-order"));
			TemplateEngine engine = new TemplateEngine();
			engine.addTemplateResolver(templateResolver);
			return engine;
		}

		private WebContext webContext(ViewEngineContext ctx) {
			return new WebContext(ctx.getRequest(), ctx.getResponse(), ctx.getRequest().getServletContext(),
					ctx.getLocale(), ctx.getModels());
		}
		
		private TemplateEngine templateEngine = this.initTemplateEngine();

		/**
		 * Renders the view using Thymeleaf TemplateEngine.
		 */
		public void processView(ViewEngineContext ctx) throws ViewEngineException {
			String view = ctx.getView();
			LOGGER.debug("Processing view:[{}]", view);
			try {
				this.templateEngine.process(view, this.webContext(ctx), ctx.getResponse().getWriter());
			} catch (IOException ex) {
				LOGGER.error("IOException while processing view: [{}]", view, ex);
				throw new ViewEngineException(ex.getMessage(), ex);
			}
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngines.class);

	public abstract void processView(ViewEngineContext engineContext) throws ViewEngineException;
}
