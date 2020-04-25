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

package com.adeptj.runtime.templating;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.servlet.http.HttpServletResponse;

/**
 * TemplateContext containing required objects for template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateEngineContext {

    private final String template;

    private final HttpServletResponse response;

    private TemplateData templateData;

    private TemplateEngineContext(String template, HttpServletResponse resp) {
        this.template = template;
        this.response = resp;
    }

    String getTemplate() {
        return template;
    }

    TemplateData getTemplateData() {
        return templateData;
    }

    HttpServletResponse getResponse() {
        return response;
    }

    public static Builder builder(String template, HttpServletResponse resp) {
        return new Builder(template, resp);
    }

    /**
     * Builder for TemplateContext.
     *
     * @author Rakesh.Kumar, AdeptJ
     */
    public static class Builder {

        private final String template;

        private TemplateData templateData;

        private final HttpServletResponse response;

        private Builder(String template, HttpServletResponse resp) {
            Validate.isTrue(StringUtils.isNotEmpty(template), "Template name can't be null!");
            this.template = template;
            this.response = resp;
        }

        public Builder templateData(TemplateData templateData) {
            this.templateData = templateData;
            return this;
        }

        public TemplateEngineContext build() {
            TemplateEngineContext context = new TemplateEngineContext(this.template, this.response);
            context.templateData = this.templateData;
            return context;
        }
    }
}
