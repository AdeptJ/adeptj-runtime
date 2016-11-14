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

import com.adeptj.runtime.admin.render.ContextObjects;
import com.adeptj.runtime.admin.render.RenderContext;
import com.adeptj.runtime.admin.render.RenderEngine;
import com.adeptj.runtime.config.Configs;

/**
 * OSGiGenericErrorSevlet handles the system wide error codes and exceptions.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "OSGiGenericErrorSevlet")
public class OSGiGenericErrorSevlet extends HttpServlet {

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
		RenderContext.Builder builder = new RenderContext.Builder();
		builder.contextObjects(this.contextObjects(req, statusCode)).request(req).response(resp).locale(req.getLocale());
		if (Integer.valueOf(500).equals(statusCode)) {
			RenderEngine.INSTANCE.render(builder.view("error/500").build());
		} else if (Configs.INSTANCE.undertow().getIntList("common.status-codes").contains(statusCode)) {
			RenderEngine.INSTANCE.render(builder.view(String.format("error/%s", statusCode)).build());
		} else {
			RenderEngine.INSTANCE.render(builder.view("error/generic").build());
		}
	}

	private ContextObjects contextObjects(HttpServletRequest req, Integer statusCode) {
		ContextObjects contextObjects = new ContextObjects();
		contextObjects.put("statusCode", statusCode);
		contextObjects.put("errorMsg", req.getAttribute(RequestDispatcher.ERROR_MESSAGE));
		contextObjects.put("reqURI", req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
		contextObjects.put("exception", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
		return contextObjects;
	}
}
