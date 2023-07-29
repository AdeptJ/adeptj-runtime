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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * TemplateEngineContext containing required objects for template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateEngineContext {

    private final String template;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private Map<String, Object> templateVariables;

    private TemplateEngineContext(String template, HttpServletRequest request, HttpServletResponse response) {
        this.template = template;
        this.request = request;
        this.response = response;
    }

    String getTemplate() {
        return this.template;
    }

    HttpServletRequest getRequest() {
        return this.request;
    }

    HttpServletResponse getResponse() {
        return this.response;
    }

    Map<String, Object> getTemplateVariables() {
        return this.templateVariables;
    }

    public static Builder builder(String template, HttpServletRequest request, HttpServletResponse response) {
        return new Builder(template, request, response);
    }

    /**
     * Builder for TemplateContext.
     *
     * @author Rakesh.Kumar, AdeptJ
     */
    public static class Builder {

        private final String template;

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        private final Map<String, Object> templateVariables;

        private Builder(String template, HttpServletRequest request, HttpServletResponse response) {
            Validate.isTrue(StringUtils.isNotEmpty(template), "Template name can't be null!");
            this.template = template;
            this.request = request;
            this.response = response;
            this.templateVariables = new HashMap<>();
        }

        public Builder addTemplateVariable(String key, Object value) {
            this.templateVariables.put(key, value);
            return this;
        }

        public TemplateEngineContext build() {
            TemplateEngineContext context = new TemplateEngineContext(this.template, this.request, this.response);
            context.templateVariables = this.templateVariables;
            return context;
        }
    }
}
