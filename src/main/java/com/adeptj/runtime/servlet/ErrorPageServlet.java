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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.templating.ContextObject;
import com.adeptj.runtime.templating.TemplateContext;
import com.adeptj.runtime.templating.TemplateEngine;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ErrorPageServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.) for admin related operations.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "ErrorPageServlet", urlPatterns = {"/tools/error/*"})
public class ErrorPageServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        TemplateContext.Builder builder = new TemplateContext.Builder(req, resp);
        ContextObject ctxObj = new ContextObject();
        TemplateEngine templateEngine = TemplateEngine.instance();
		if ("/admin/error".equals(requestURI)) {
        	templateEngine.render(builder.template("error/generic").build());
        } else {
            Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            String statusCode = this.getStatusCode(requestURI);
            if (exception != null && "500".equals(statusCode)) {
                builder.contextObject(ctxObj.put("exception", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION)));
                templateEngine.render(builder.template("error/500").build());
            } else if (Configs.DEFAULT.undertow().getStringList("common.status-codes").contains(statusCode)) {
            	templateEngine.render(builder.template(String.format("error/%s", statusCode)).build());
            } else {
                // if the requested view not found, render 404.
            	templateEngine.render(builder.template("error/404").build());
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