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

import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.viewengine.Models;
import com.adeptj.runtime.viewengine.ViewEngine;
import com.adeptj.runtime.viewengine.ViewEngineContext;

/**
 * PerContextErrorServlet handles the error codes and exceptions for each ServletContext registered with OSGi.
 * 
 * Note: This is independent of UndertowServer and directly managed by OSGi. 
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "PerContextErrorServlet", asyncSupported = true)
public class PerContextErrorServlet extends HttpServlet {

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
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder();
		builder.models(this.models(req, statusCode)).request(req).response(resp);
		if (Configs.INSTANCE.undertow().getIntList("common.status-codes").contains(statusCode)) {
			ViewEngine.INSTANCE.processView(builder.view(String.format("error/%s", statusCode)).build());
		} else {
			// if the requested view not found, render 404.
			ViewEngine.INSTANCE.processView(builder.view("error/404").build());
		}
	}

	private Models models(HttpServletRequest req, Integer statusCode) {
		Models models = new Models();
		models.put("statusCode", statusCode);
		models.put("errorMsg", req.getAttribute(RequestDispatcher.ERROR_MESSAGE));
		models.put("reqURI", req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
		models.put("exception", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
		return models;
	}
}
