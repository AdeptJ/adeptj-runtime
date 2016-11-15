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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.runtime.viewengine.ViewEngineContext;
import com.adeptj.runtime.viewengine.Models;
import com.adeptj.runtime.viewengine.ViewEngine;

/**
 * AdminLoginServlet serves the login page and handles the validation failure on wrong credentials submission.
 * 
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdminLoginServlet", urlPatterns = { "/admin/login" })
public class AdminLoginServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	/**
	 * Render login page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder();
		builder.view("auth/login").models(new Models()).request(req).response(resp).locale(req.getLocale());
		ViewEngine.INSTANCE.processView(builder.build());
	}

	/**
	 * Control comes here when login to "/auth/j_security_check" fails due to invalid credentials.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder();
		Models models = new Models();
		models.put("validation", "Invalid credentials!!");
		models.put("j_username", req.getParameter("j_username"));
		builder.view("auth/login").models(models).request(req).response(resp).locale(req.getLocale());
		// Render login page again with validation message.
		ViewEngine.INSTANCE.processView(builder.build());
	}
}