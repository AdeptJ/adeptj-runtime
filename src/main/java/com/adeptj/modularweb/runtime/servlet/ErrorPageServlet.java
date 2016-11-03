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
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.modularweb.runtime.common.CommonUtils;

/**
 * OSGi ErrorPageServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.).
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ErrorPageServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		ServletOutputStream outputStream = resp.getOutputStream();
		if ("/error".equals(requestURI)) {
			outputStream.write(CommonUtils.toBytes(getClass().getResourceAsStream("/views/error/generic.html")));
		} else {
			InputStream resource = getClass()
					.getResourceAsStream(String.format("/views/error/%s.html", this.getStatusCode(requestURI)));
			if (resource == null) {
				outputStream.write(CommonUtils.toBytes(getClass().getResourceAsStream("/views/error/404.html")));
			} else {
				outputStream.write(CommonUtils.toBytes(resource));
			}
		}
	}

	private String getStatusCode(String requestURI) {
		return requestURI.substring(requestURI.lastIndexOf('/') + 1);
	}
}