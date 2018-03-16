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

import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.config.ViewEngineConfig;
import com.typesafe.config.ConfigBeanFactory;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.handlebars.i18n.ResourceBundleHelper;

import static com.adeptj.runtime.common.Constants.UTF8;
import static org.trimou.engine.config.EngineConfigurationKey.*;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_EXPIRATION_TIMEOUT;
import static org.trimou.handlebars.i18n.ResourceBundleHelper.Format.MESSAGE;

/**
 * Utility methods for Trimou {@link MustacheEngine}
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateEngines {

    private static final String RB_HELPER_NAME = "msg";


    public static TemplateEngine getDefault() {
        return DefaultTemplateEngine.INSTANCE;
    }

    static MustacheEngine buildMustacheEngine() {
        ViewEngineConfig config = ConfigBeanFactory.create(Configs.DEFAULT.trimou(), ViewEngineConfig.class);
        return MustacheEngineBuilder.newBuilder()
                .registerHelper(RB_HELPER_NAME, new ResourceBundleHelper(config.getResourceBundleBasename(), MESSAGE))
                .addTemplateLocator(templateLocator(config))
                .setProperty(START_DELIMITER, config.getStartDelimiter())
                .setProperty(END_DELIMITER, config.getEndDelimiter())
                .setProperty(DEFAULT_FILE_ENCODING, UTF8)
                .setProperty(TEMPLATE_CACHE_ENABLED, config.isCacheEnabled())
                .setProperty(TEMPLATE_CACHE_EXPIRATION_TIMEOUT, config.getCacheExpiration())
                .build();
    }

    private static TemplateLocator templateLocator(ViewEngineConfig config) {
        return ClassPathTemplateLocator.builder()
                .setPriority(config.getTemplateLocatorPriority())
                .setRootPath(config.getPrefix())
                .setSuffix(config.getSuffix())
                .setScanClasspath(false)
                .setClassLoader(DefaultTemplateEngine.class.getClassLoader())
                .build();
    }
}
