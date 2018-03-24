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

import com.adeptj.runtime.common.ResponseUtil;
import com.adeptj.runtime.common.Times;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders Html Templates using Trimou {@link MustacheEngine}
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum DefaultTemplateEngine implements TemplateEngine {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTemplateEngine.class);

    private final MustacheEngine mustacheEngine;

    DefaultTemplateEngine() {
        long startTime = System.nanoTime();
        this.mustacheEngine = TemplateEngines.buildMustacheEngine();
        // Can't use the static LOGGER due to enum restriction.
        LoggerFactory.getLogger(DefaultTemplateEngine.class).info("MustacheEngine initialized in: [{}] ms!!",
                Times.elapsedMillis(startTime));
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
                ResponseUtil.sendError(context.getResponse(), SC_NOT_FOUND);
            } else {
                context.getResponse().getWriter().write(template);
            }
        } catch (Exception ex) { // NOSONAR
            LOGGER.error(ex.getMessage(), ex);
            context.getRequest().setAttribute(ERROR_EXCEPTION, ex);
            ResponseUtil.sendError(context.getResponse(), SC_INTERNAL_SERVER_ERROR);
        }
    }
}