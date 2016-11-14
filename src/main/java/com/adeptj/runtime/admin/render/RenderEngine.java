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
package com.adeptj.runtime.admin.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;

import com.adeptj.runtime.common.TimeUnits;
import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;

/**
 * RenderEngine.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum RenderEngine {

	INSTANCE;

	private static final Logger LOGGER = LoggerFactory.getLogger(RenderEngine.class);

	private MustacheEngine engine;

	private void initMustacheEngine() {
		if (this.engine == null) {
			long startTime = System.nanoTime();
			Config config = Configs.INSTANCE.common();
			this.engine = MustacheEngineBuilder.newBuilder()
					.addTemplateLocator(new ClassPathTemplateLocator(config.getInt("admin-template-locator-priority"),
							config.getString("admin-view-root"), config.getString("admin-view-suffix"),
							RenderEngine.class.getClassLoader(), false)).build();
			LoggerFactory.getLogger(RenderEngine.class).info("MustacheEngine initialization took: [{}] ms!!", TimeUnits.nanosToMillis(startTime));
		}
	}

	RenderEngine() {
		this.initMustacheEngine();
	}

	public void render(RenderContext context) throws RenderException {
		String view = context.getView();
		LOGGER.debug("Rendering view:[{}]", view);
		try {
			context.getResponse().getWriter()
					.write(this.engine.getMustache(view).render(context.getContextObjects()));
		} catch (Exception ex) {
			LOGGER.error("Exception while processing view: [{}]", view, ex);
			throw new RenderException(ex.getMessage(), ex);
		}
	}
}
