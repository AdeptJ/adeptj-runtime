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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.Requests;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * ErrorPageServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.) for admin related operations.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ ErrorPageServlet", urlPatterns = {"/tools/error/*"}, asyncSupported = true)
public class ErrorPageServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    private static final String STATUS_500 = "500";

    private static final String TEMPLATE_ERROR = "/tools/error";

    private static final String KEY_EXCEPTION = "exception";

    private static final String TEMPLATE_404 = "error/404";

    private static final String TEMPLATE_500 = "error/500";

    private static final String TEMPLATE_GENERIC = "error/generic";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    private static final String TEMPLATE_ERROR_RESOLVABLE = "error/%s";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        TemplateContext.Builder builder = TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale());
        TemplateEngine templateEngine = TemplateEngine.defaultEngine();
        if (TEMPLATE_ERROR.equals(requestURI)) {
            templateEngine.render(builder.template(TEMPLATE_GENERIC).build());
        } else {
            ContextObject ctxObj = new ContextObject();
            String statusCode = this.getStatusCode(requestURI);
            if (Requests.hasException(req) && STATUS_500.equals(statusCode)) {
                builder.contextObject(ctxObj.put(KEY_EXCEPTION, Requests.attr(req, ERROR_EXCEPTION)));
                templateEngine.render(builder.template(TEMPLATE_500).build());
            } else if (STATUS_500.equals(statusCode)) {
                // Means it's just error code, no exception set in the request.
                templateEngine.render(builder.template(TEMPLATE_GENERIC).build());
            } else if (Configs.DEFAULT.undertow().getStringList(KEY_STATUS_CODES).contains(statusCode)) {
                templateEngine.render(builder.template(String.format(TEMPLATE_ERROR_RESOLVABLE, statusCode)).build());
            } else {
                // if the requested view not found, render 404.
                templateEngine.render(builder.template(TEMPLATE_404).build());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private String getStatusCode(String requestURI) {
        return requestURI.substring(requestURI.lastIndexOf('/') + 1);
    }
}