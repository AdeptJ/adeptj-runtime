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

import com.adeptj.runtime.common.Constants;
import com.adeptj.runtime.viewengine.Models;
import com.adeptj.runtime.viewengine.ViewEngine;
import com.adeptj.runtime.viewengine.ViewEngineContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.adeptj.runtime.common.Constants.ADMIN_LOGIN_URI;
import static com.adeptj.runtime.common.Constants.ADMIN_LOGOUT_URI;

/**
 * AdminAuthServlet does the following: 
 * 
 * 1. Serves the login page and handles the validation failure on wrong credentials submission.
 * 2. Logout the currently logged in Admin user and renders the login page again.
 * 
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdminAuthServlet", urlPatterns = { ADMIN_LOGIN_URI, ADMIN_LOGOUT_URI })
public class AdminAuthServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	/**
	 * Render login page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		if (ADMIN_LOGIN_URI.equals(requestURI)) {
			this.renderLoginPage(req, resp);
		} else if (ADMIN_LOGOUT_URI.equals(requestURI) && req.isUserInRole(Constants.OSGI_WEBCONSOLE_ROLE)) {
			this.logout(req, resp);
		} else {
			// if someone requesting logout URI anonymously, which doesn't make sense. Redirect to /system/console.
			resp.sendRedirect(Constants.OSGI_WEBCONSOLE_URI);
		}
	}

	/**
	 * Handle "/auth/j_security_check" validation failure.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.handleLoginFailure(req, resp);
	}

	private void handleLoginFailure(HttpServletRequest req, HttpServletResponse resp) {
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder(req, resp);
		Models models = new Models();
		models.put("validation", "Invalid credentials!!");
		models.put("j_username", req.getParameter("j_username"));
		// Render login page again with validation message.
		ViewEngine.INSTANCE.processView(builder.view("auth/login").models(models).build());
	}

	private void renderLoginPage(HttpServletRequest req, HttpServletResponse resp) {
		ViewEngine.INSTANCE.processView(new ViewEngineContext.Builder(req, resp).view("auth/login").build());
	}

	private void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Invalidate the session and redirect back to /system/console page.
		req.logout();
		resp.sendRedirect(Constants.OSGI_WEBCONSOLE_URI);
	}
}