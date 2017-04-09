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
package com.adeptj.runtime.templating;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * TemplateContext.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class TemplateContext {

    private final String template;

    private final ContextObject contextObject;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private Locale locale;

    private boolean templateRendered;

    private TemplateContext(String template, ContextObject contextObject, HttpServletRequest req, HttpServletResponse resp) {
        this.template = template;
        this.contextObject = contextObject;
        this.request = req;
        this.response = resp;
    }

    public String getTemplate() {
        return template;
    }

    public ContextObject getContextObject() {
        return contextObject;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isTemplateRendered() {
        return templateRendered;
    }

    public void setTemplateRendered(boolean templateRendered) {
        this.templateRendered = templateRendered;
    }

    /**
     * Builder for TemplateContext.
     *
     * @author Rakesh.Kumar, AdeptJ.
     */
    public static class Builder {

        private String template;

        private ContextObject contextObject;

        private Locale locale;

        private HttpServletRequest request;

        private HttpServletResponse response;

        /**
         * Initialize the Builder with mandatory request and response.
         *
         * @param request  the {@link HttpServletRequest}
         * @param response the {@link HttpServletResponse}
         */
        public Builder(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
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
            TemplateContext context = new TemplateContext(this.template, this.contextObject, this.request, this.response);
            // English is default Locale if no locale set.
            context.locale = this.locale == null ? Locale.ENGLISH : this.locale;
            return context;
        }
    }
}
