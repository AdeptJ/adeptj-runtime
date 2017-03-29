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
package com.adeptj.runtime.viewengine;

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
 * ViewEngine. Rendering Html Templates
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum ViewEngine {

    TRIMOU;

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewEngine.class);

    private static final String RB_HELPER_NAME = "msg";

    private final MustacheEngine engine;

    ViewEngine() {
        long startTime = System.nanoTime();
        this.engine = this.mustacheEngine();
        LoggerFactory.getLogger(ViewEngine.class).info("MustacheEngine initialized in: [{}] ms!!", Times.elapsedSinceMillis(startTime));
    }

    private TemplateLocator templateLocator(ViewEngineConfig config) {
        return new ClassPathTemplateLocator(config.getTemplateLocatorPriority(), config.getPrefix(),
                config.getSuffix(), ViewEngine.class.getClassLoader(), false);
    }

    private Helper resourceBundleHelper(ViewEngineConfig config) {
        return new ResourceBundleHelper(config.getResourceBundleBasename(), Format.MESSAGE);
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

    public void processView(ViewEngineContext context) {
        this.processViewInternal(context);
    }

    private void sendError(HttpServletResponse resp, int errorCode) {
        LOGGER.info("Sending error: [{}]", errorCode);
        try {
            resp.sendError(errorCode);
        } catch (IOException ex) {
            // Now what? may be log and re-throw. Let container handle it.
            LOGGER.error("Exception while sending error!!", ex);
            throw new ViewEngineException(ex.getMessage(), ex);
        }
    }

    private void handleException(ViewEngineContext context, Exception ex) {
        context.getRequest().setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);
        this.sendError(context.getResponse(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void handleViewNotFound(ViewEngineContext context) {
        // Check if the view not rendered and it was not an exception which is set as request attribute.
        if (!context.isViewRendered() && context.getRequest().getAttribute(RequestDispatcher.ERROR_EXCEPTION) == null) {
            LOGGER.info("View not found: [{}]", context.getView());
            this.sendError(context.getResponse(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void processViewInternal(ViewEngineContext context) {
        Optional.ofNullable(this.engine.getMustache(context.getView())).ifPresent(mustache -> this.render(context, mustache));
        // Now check if the view actually rendered(may not due to a 404), if not then handle the 404 response properly.
        this.handleViewNotFound(context);
    }

    private void render(ViewEngineContext context, Mustache mustache) {
        try {
            long startTime = System.nanoTime();
            context.getResponse().getWriter().write(mustache.render(context.getModels()));
            LOGGER.debug("Processed view: [{}] in: [{}] ms!!", context.getView(), Times.elapsedSinceMillis(startTime));
            // if we are here means view rendered properly, set the ViewEngineContext#viewRendered attribute.
            context.setViewRendered(true);
        } catch (Exception ex) { // NOSONAR
            LOGGER.error("Exception while processing view: [{}]", context.getView(), ex);
            this.handleException(context, ex);
        }
    }
}