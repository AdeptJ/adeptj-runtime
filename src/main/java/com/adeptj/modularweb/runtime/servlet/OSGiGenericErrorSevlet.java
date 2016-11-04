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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.modularweb.runtime.common.CommonUtils;

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
		// String errorMsg = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		// String reqURI = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
		// String servletName = (String) req.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME);
		Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		ServletOutputStream outputStream = resp.getOutputStream();
		if (exception != null && Integer.valueOf(500).equals(statusCode)) {
			outputStream.write(CommonUtils.toString(getClass().getResourceAsStream("/admin/views/error/500.html"))
					.replace("#{error}", exception.toString()).getBytes("UTF-8"));
		} else if (Integer.valueOf(401).equals(statusCode) || Integer.valueOf(403).equals(statusCode)
				|| Integer.valueOf(500).equals(statusCode)) {
			outputStream.write(CommonUtils
					.toBytes(getClass().getResourceAsStream(String.format("/admin/views/error/%s.html", statusCode))));
		} else {
			outputStream.write(CommonUtils.toBytes(getClass().getResourceAsStream("/admin/views/error/generic.html")));
		}
	}
}
