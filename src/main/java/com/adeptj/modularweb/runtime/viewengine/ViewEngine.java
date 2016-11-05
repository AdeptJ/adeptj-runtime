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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * ViewEngine.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ViewEngine {

	THYMELEAF {

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

		private TemplateEngine initTemplateEngine() {
			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("admin/views/");
			templateResolver.setSuffix(".html");
			templateResolver.setCharacterEncoding("UTF-8");
			templateResolver.setTemplateMode(TemplateMode.HTML);
			templateResolver.setCacheable(true);
			// Template cache TTL=1h
			templateResolver.setCacheTTLMs(Long.valueOf(3600000L));
			templateResolver.setOrder(1);
			TemplateEngine engine = new TemplateEngine();
			engine.addTemplateResolver(templateResolver);
			return engine;
		}

		private WebContext webContext(ViewEngineContext ctx) {
			return new WebContext(ctx.getRequest(), ctx.getResponse(), ctx.getRequest().getServletContext(),
					ctx.getLocale(), ctx.getModels());
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngine.class);

	public abstract void processView(ViewEngineContext engineContext) throws ViewEngineException;
}
