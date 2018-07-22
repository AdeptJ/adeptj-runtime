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

package io.adeptj.runtime.tools;

import com.typesafe.config.ConfigBeanFactory;
import io.adeptj.runtime.common.Times;
import io.adeptj.runtime.config.Configs;
import io.adeptj.runtime.config.ViewEngineConfig;
import org.slf4j.LoggerFactory;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.engine.locator.TemplateLocator;
import org.trimou.handlebars.i18n.ResourceBundleHelper;

import static io.adeptj.runtime.common.Constants.UTF8;
import static org.trimou.engine.config.EngineConfigurationKey.DEFAULT_FILE_ENCODING;
import static org.trimou.engine.config.EngineConfigurationKey.END_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.START_DELIMITER;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_ENABLED;
import static org.trimou.engine.config.EngineConfigurationKey.TEMPLATE_CACHE_EXPIRATION_TIMEOUT;
import static org.trimou.handlebars.i18n.ResourceBundleHelper.Format.MESSAGE;

/**
 * Utility methods for Trimou {@link MustacheEngine}
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateEngines {

    private static final String RB_HELPER_NAME = "msg";

    // static utility methods only
    private TemplateEngines() {
    }

    public static TemplateEngine getEngine() {
        return DefaultTemplateEngine.getInstance();
    }

    static MustacheEngine newMustacheEngine() {
        long startTime = System.nanoTime();
        ViewEngineConfig config = ConfigBeanFactory.create(Configs.of().trimou(), ViewEngineConfig.class);
        MustacheEngine engine = MustacheEngineBuilder.newBuilder()
                .registerHelper(RB_HELPER_NAME, new ResourceBundleHelper(config.getResourceBundleBasename(), MESSAGE))
                .addTemplateLocator(newTemplateLocator(config))
                .setProperty(START_DELIMITER, config.getStartDelimiter())
                .setProperty(END_DELIMITER, config.getEndDelimiter())
                .setProperty(DEFAULT_FILE_ENCODING, UTF8)
                .setProperty(TEMPLATE_CACHE_ENABLED, config.isCacheEnabled())
                .setProperty(TEMPLATE_CACHE_EXPIRATION_TIMEOUT, config.getCacheExpiration())
                .build();
        LoggerFactory.getLogger(TemplateEngines.class)
                .info("MustacheEngine initialized in: [{}] ms!!", Times.elapsedMillis(startTime));
        return engine;
    }

    private static TemplateLocator newTemplateLocator(ViewEngineConfig config) {
        return ClassPathTemplateLocator.builder()
                .setPriority(config.getTemplateLocatorPriority())
                .setRootPath(config.getPrefix())
                .setSuffix(config.getSuffix())
                .setScanClasspath(false)
                .setClassLoader(TemplateEngines.class.getClassLoader())
                .build();
    }
}
