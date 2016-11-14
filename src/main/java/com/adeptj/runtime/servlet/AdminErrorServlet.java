/** 
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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.runtime.viewengine.ViewEngineContext;
import com.adeptj.runtime.viewengine.Models;
import com.adeptj.runtime.viewengine.ViewEngine;

/**
 * AdminErrorServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.) for admin related operations.
 * 
 * Note: This is independent of OSGi and directly managed by Undertow. 
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdminErrorServlet", urlPatterns = { "/admin/error/*" })
public class AdminErrorServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder();
		Models models = new Models();
		builder.models(models).request(req).response(resp).locale(req.getLocale());
		if ("/admin/error".equals(requestURI)) {
			ViewEngine.INSTANCE.processView(builder.view("error/generic").build());
		} else {
			Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
			String statusCode = this.getStatusCode(requestURI);
			if (exception != null && "500".equals(statusCode)) {
				models.put("exception", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
				ViewEngine.INSTANCE.processView(builder.view("error/500").build());
			} else if (!ViewEngine.INSTANCE.processView(builder.view(String.format("error/%s", statusCode)).build())) {
				// if the requested view not found, render 404.
				ViewEngine.INSTANCE.processView(builder.view("error/404").build());
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