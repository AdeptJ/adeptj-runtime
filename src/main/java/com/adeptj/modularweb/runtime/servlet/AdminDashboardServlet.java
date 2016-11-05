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
package com.adeptj.modularweb.runtime.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.modularweb.runtime.viewengine.Models;
import com.adeptj.modularweb.runtime.viewengine.ViewEngineContext;
import com.adeptj.modularweb.runtime.viewengine.ViewEngines;

/**
 * OSGi AdminDashboardServlet renders the admin dashboard page.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = { "/admin/dashboard/*" })
public class AdminDashboardServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	/**
	 * Render dashboard page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ViewEngineContext.Builder builder = new ViewEngineContext.Builder();
		builder.view("auth/dashboard").models(new Models()).request(req).response(resp).locale(req.getLocale());
		ViewEngines.THYMELEAF.processView(builder.build());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/admin/dashboard");
	}
}