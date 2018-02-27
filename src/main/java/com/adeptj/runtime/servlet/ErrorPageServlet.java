/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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
@WebServlet(
        name = "AdeptJ ErrorPageServlet",
        urlPatterns = {
                "/tools/error/*"
        },
        asyncSupported = true
)
public class ErrorPageServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        ErrorPageUtil.renderErrorPage(req, resp);
    }
}