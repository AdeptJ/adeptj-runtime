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

package com.adeptj.runtime.tools;

import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.config.ViewEngineConfig;
import com.adeptj.runtime.servlet.ErrorPageUtil;
import com.typesafe.config.ConfigBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.handlebars.Helper;
import org.trimou.handlebars.i18n.ResourceBundleHelper;

import static com.adeptj.runtime.common.Constants.UTF8;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.trimou.engine.config.EngineConfigurationKey.DEFAULT_FILE_ENCODING;
import static org.trimou.engine.config.EngineConfigurationKey.END_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.START_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_ENABLED;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_EXPIRATION_TIMEOUT;
import static org.trimou.handlebars.i18n.ResourceBundleHelper.Format.MESSAGE;

/**
 * Renders Html Templates using Trimou {@link MustacheEngine}
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum DefaultTemplateEngine implements TemplateEngine {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTemplateEngine.class);

    private static final String RB_HELPER_NAME = "msg";

    private final MustacheEngine mustacheEngine;

    DefaultTemplateEngine() {
        long startTime = System.nanoTime();
        this.mustacheEngine = this.buildMustacheEngine();
        LoggerFactory.getLogger(DefaultTemplateEngine.class)
                .info("MustacheEngine initialized in: [{}] ms!!", Times.elapsedMillis(startTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(TemplateContext context) {
        try {
            Mustache mustache = this.mustacheEngine.getMustache(context.getTemplate());
            String template = mustache == null ? null : mustache.render(context.getContextObject());
            if (StringUtils.isEmpty(template)) {
                LOGGER.error("Template not found: [{}]", context.getTemplate());
                ErrorPageUtil.sendError(context.getResponse(), SC_NOT_FOUND);
            } else {
                context.getResponse().getWriter().write(template);
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error(ex.getMessage(), ex);
            this.handleException(context, ex);
        }
    }

    private void handleException(TemplateContext context, Exception ex) {
        context.getRequest().setAttribute(ERROR_EXCEPTION, ex);
        ErrorPageUtil.sendError(context.getResponse(), SC_INTERNAL_SERVER_ERROR);
    }

    private MustacheEngine buildMustacheEngine() {
        ViewEngineConfig config = ConfigBeanFactory.create(Configs.DEFAULT.trimou(), ViewEngineConfig.class);
        return MustacheEngineBuilder.newBuilder()
                .registerHelper(RB_HELPER_NAME, this.resourceBundleHelper(config))
                .addTemplateLocator(this.templateLocator(config))
                .setProperty(START_DELIMITER, config.getStartDelimiter())
                .setProperty(END_DELIMITER, config.getEndDelimiter())
                .setProperty(DEFAULT_FILE_ENCODING, UTF8)
                .setProperty(TEMPLATE_CACHE_ENABLED, config.isCacheEnabled())
                .setProperty(TEMPLATE_CACHE_EXPIRATION_TIMEOUT, config.getCacheExpiration())
                .build();
    }

    private TemplateLocator templateLocator(ViewEngineConfig config) {
        return ClassPathTemplateLocator.builder()
                .setPriority(config.getTemplateLocatorPriority())
                .setRootPath(config.getPrefix())
                .setSuffix(config.getSuffix())
                .setScanClasspath(false)
                .setClassLoader(DefaultTemplateEngine.class.getClassLoader())
                .build();
    }

    private Helper resourceBundleHelper(ViewEngineConfig config) {
        return new ResourceBundleHelper(config.getResourceBundleBasename(), MESSAGE);
    }
}