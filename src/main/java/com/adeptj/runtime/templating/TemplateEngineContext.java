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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static java.util.Locale.ENGLISH;

/**
 * TemplateContext containing required objects for template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateEngineContext {

    private final String template;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private TemplateData templateData;

    private Locale locale;

    private TemplateEngineContext(String template, HttpServletRequest req, HttpServletResponse resp) {
        Validate.isTrue(StringUtils.isNotEmpty(template), "Template name can't be null!");
        this.template = template;
        this.request = req;
        this.response = resp;
    }

    String getTemplate() {
        return template;
    }

    TemplateData getTemplateData() {
        return templateData;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    HttpServletResponse getResponse() {
        return response;
    }

    public Locale getLocale() {
        return locale;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for TemplateContext.
     *
     * @author Rakesh.Kumar, AdeptJ
     */
    public static class Builder {

        private String template;

        private TemplateData templateData;

        private Locale locale;

        private HttpServletRequest request;

        private HttpServletResponse response;

        private Builder() {
        }

        public Builder request(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public Builder response(HttpServletResponse response) {
            this.response = response;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public Builder templateData(TemplateData templateData) {
            this.templateData = templateData;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public TemplateEngineContext build() {
            TemplateEngineContext context = new TemplateEngineContext(this.template, this.request, this.response);
            context.templateData = this.templateData;
            // English is default Locale if no locale set.
            context.locale = this.locale == null ? ENGLISH : this.locale;
            return context;
        }
    }
}
