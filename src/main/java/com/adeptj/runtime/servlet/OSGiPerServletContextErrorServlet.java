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
 * OSGiPerServletContextErrorServlet handles the error codes and exceptions for each ServletContext registered with OSGi.
 * <p><b>
 * Note: This is independent of UndertowServer and directly managed by OSGi.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "OSGiPerServletContextErrorServlet", asyncSupported = true)
public class OSGiPerServletContextErrorServlet extends HttpServlet {

    private static final long serialVersionUID = -5818850813832379842L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer statusCode = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        TemplateContext.Builder builder = new TemplateContext.Builder(req, resp);
        ContextObject ctxObj = this.contextObject(req, statusCode);
        builder.contextObject(ctxObj);
        TemplateEngine templateEngine = TemplateEngine.instance();
		if (ctxObj.get("exception") != null && Integer.valueOf(500).equals(statusCode)) {
        	templateEngine.render(builder.template("error/500").build());
        } else if (Configs.DEFAULT.undertow().getIntList("common.status-codes").contains(statusCode)) {
        	templateEngine.render(builder.template(String.format("error/%s", statusCode)).build());
        } else {
            // if the requested view not found, render 404.
        	templateEngine.render(builder.template("error/404").build());
        }
    }

	private ContextObject contextObject(HttpServletRequest req, Integer statusCode) {
		return new ContextObject().put("statusCode", statusCode).put("errorMsg", req.getAttribute(RequestDispatcher.ERROR_MESSAGE))
				.put("reqURI", req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI))
				.put("exception", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
	}
}
