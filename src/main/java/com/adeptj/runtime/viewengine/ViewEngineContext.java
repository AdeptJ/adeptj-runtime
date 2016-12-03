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
package com.adeptj.runtime.viewengine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * ViewEngineContext.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class ViewEngineContext {

    private final String view;

    private final Models models;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private Locale locale;

    private boolean viewRendered;

    private ViewEngineContext(String view, Models models, HttpServletRequest req, HttpServletResponse resp) {
        this.view = view;
        this.models = models;
        this.request = req;
        this.response = resp;
    }

    public String getView() {
        return view;
    }

    public Models getModels() {
        return models;
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

    public boolean isViewRendered() {
        return viewRendered;
    }

    public void setViewRendered(boolean viewRendered) {
        this.viewRendered = viewRendered;
    }

    /**
     * Builder for ViewEngineContext.
     *
     * @author Rakesh.Kumar, AdeptJ.
     */
    public static class Builder {

        private String view;

        private Models models;

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

        public Builder view(String view) {
            this.view = view;
            return this;
        }

        public Builder models(Models models) {
            this.models = models;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public ViewEngineContext build() {
            ViewEngineContext context = new ViewEngineContext(this.view, this.models, this.request, this.response);
            // English is default Locale if no locale set.
            context.locale = this.locale == null ? Locale.ENGLISH : this.locale;
            return context;
        }
    }
}
