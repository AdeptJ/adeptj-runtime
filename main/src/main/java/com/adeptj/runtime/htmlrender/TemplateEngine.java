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

package com.adeptj.runtime.htmlrender;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

import static com.adeptj.runtime.common.Constants.CONTENT_TYPE_HTML_UTF8;
import static com.adeptj.runtime.common.Constants.PEBBLE_CONF_SECTION;

/**
 * Pebble based TemplateEngine for rendering the HTML templates.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum TemplateEngine {

    INSTANCE;

    private static final String TEMPLATE_ENGINE_INIT_MSG = "TemplateEngine initialized in [{}] ms!!";

    private static final String KEY_CACHE_ENABLED = "cache-enabled";

    private static final String KEY_STRICT_VARIABLES = "strict-variables";

    private static final String KEY_PREFIX = "prefix";

    private static final String KEY_SUFFIX = "suffix";

    private static final String KEY_RB_DIR = "resource-bundle-dir";

    private final PebbleEngine pebbleEngine;

    TemplateEngine() {
        long startTime = System.nanoTime();
        Config pebbleConfig = ConfigProvider.getInstance().getMainConfig().getConfig(PEBBLE_CONF_SECTION);
        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix(pebbleConfig.getString(KEY_PREFIX));
        loader.setSuffix(pebbleConfig.getString(KEY_SUFFIX));
        this.pebbleEngine = new PebbleEngine.Builder()
                .cacheActive(pebbleConfig.getBoolean(KEY_CACHE_ENABLED))
                .strictVariables(pebbleConfig.getBoolean(KEY_STRICT_VARIABLES))
                .extension(new I18nSupport(pebbleConfig.getString(KEY_RB_DIR)))
                .loader(loader)
                .build();
        LoggerFactory.getLogger(this.getClass()).info(TEMPLATE_ENGINE_INIT_MSG, Times.elapsedMillis(startTime));
    }

    /**
     * Renders the template contained by the {@link TemplateEngineContext#getTemplate()}
     *
     * @param context the TemplateEngine context
     * @throws TemplateProcessingException if there was some issue while rendering the template.
     */
    public void render(TemplateEngineContext context) {
        // Making sure the Content-Type will always be text/html;charset=UTF-8.
        HttpServletResponse response = context.getResponse();
        response.setContentType(CONTENT_TYPE_HTML_UTF8);
        try {
            PebbleTemplate template = this.pebbleEngine.getTemplate(context.getTemplate());
            template.evaluate(response.getWriter(), context.getTemplateVariables());
        } catch (Exception ex) { // NOSONAR
            throw new TemplateProcessingException(ex);
        }
    }

    public static TemplateEngine getInstance() {
        return INSTANCE;
    }
}
