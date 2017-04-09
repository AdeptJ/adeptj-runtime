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
package com.adeptj.runtime.templating;

import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.config.ViewEngineConfig;
import com.typesafe.config.ConfigBeanFactory;
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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.trimou.engine.config.EngineConfigurationKey.END_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.START_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_ENABLED;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_EXPIRATION_TIMEOUT;

/**
 * Trimou. Rendering Html Templates
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
enum Trimou implements TemplateEngine {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Trimou.class);

    private static final String RB_HELPER_NAME = "msg";

    private final MustacheEngine engine;

    Trimou() {
        long startTime = System.nanoTime();
        this.engine = this.mustacheEngine();
        LoggerFactory.getLogger(Trimou.class).info("MustacheEngine initialized in: [{}] ms!!", Times.elapsedSinceMillis(startTime));
    }

    private MustacheEngine mustacheEngine() {
        ViewEngineConfig config = ConfigBeanFactory.create(Configs.DEFAULT.trimou(), ViewEngineConfig.class);
        return MustacheEngineBuilder.newBuilder().registerHelper(RB_HELPER_NAME, this.resourceBundleHelper(config))
                .addTemplateLocator(this.templateLocator(config))
                .setProperty(START_DELIMITER, config.getStartDelimiter())
                .setProperty(END_DELIMITER, config.getEndDelimiter())
                .setProperty(TEMPLATE_CACHE_ENABLED, config.isCacheEnabled())
                .setProperty(TEMPLATE_CACHE_EXPIRATION_TIMEOUT, config.getCacheExpiration()).build();
    }
    
    private TemplateLocator templateLocator(ViewEngineConfig config) {
        return new ClassPathTemplateLocator(config.getTemplateLocatorPriority(), config.getPrefix(),
                config.getSuffix(), Trimou.class.getClassLoader(), false);
    }

    private Helper resourceBundleHelper(ViewEngineConfig config) {
        return new ResourceBundleHelper(config.getResourceBundleBasename(), Format.MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(TemplateContext context) {
    	Optional.ofNullable(this.engine.getMustache(context.getTemplate())).ifPresent(mustache -> this.renderInternal(context, mustache));
        // Now check if the view actually rendered(may not due to a 404), if not then handle the 404 response properly.
        this.handleTemplateNotFound(context);
    }
    
    private void renderInternal(TemplateContext context, Mustache mustache) {
        try {
            long startTime = System.nanoTime();
            context.getResponse().getWriter().write(mustache.render(context.getContextObject()));
            LOGGER.debug("Processed view: [{}] in: [{}] ms!!", context.getTemplate(), Times.elapsedSinceMillis(startTime));
            // if we are here means view rendered properly, set the TemplateContext#templateRendered attribute.
            context.setTemplateRendered(true);
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while processing view: [{}]", context.getTemplate(), ex);
            this.handleException(context, ex);
        }
    }

    private void handleTemplateNotFound(TemplateContext context) {
        // Check if the view not rendered and it was not an exception which is set as request attribute.
        if (!context.isTemplateRendered() && context.getRequest().getAttribute(RequestDispatcher.ERROR_EXCEPTION) == null) {
            LOGGER.info("View not found: [{}]", context.getTemplate());
            this.sendError(context.getResponse(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleException(TemplateContext context, Exception ex) {
        context.getRequest().setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);
        this.sendError(context.getResponse(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    private void sendError(HttpServletResponse resp, int errorCode) {
        LOGGER.info("Sending error: [{}]", errorCode);
        try {
            resp.sendError(errorCode);
        } catch (IOException ex) {
            // Now what? may be log and re-throw. Let container handle it.
            LOGGER.error("Exception while sending error!!", ex);
            throw new RenderException(ex.getMessage(), ex);
        }
    }
}