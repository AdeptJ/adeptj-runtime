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
package com.adeptj.runtime.tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * TemplateContext containing required objects for template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class TemplateContext {

    private String template;

    private ContextObject contextObject;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private Locale locale;

    private TemplateContext(HttpServletRequest req, HttpServletResponse resp) {
        this.request = req;
        this.response = resp;
    }

    public String getTemplate() {
        return template;
    }

    ContextObject getContextObject() {
        return contextObject;
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

        private ContextObject contextObject;

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

        public Builder contextObject(ContextObject contextObject) {
            this.contextObject = contextObject;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public TemplateContext build() {
            TemplateContext context = new TemplateContext(this.request, this.response);
            context.template = this.template;
            context.contextObject = this.contextObject;
            // English is default Locale if no locale set.
            context.locale = this.locale == null ? Locale.ENGLISH : this.locale;
            return context;
        }
    }
}
