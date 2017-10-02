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

import com.adeptj.runtime.common.Requests;
import com.adeptj.runtime.config.Configs;

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

    private static final String STATUS_500 = "500";

    private static final String TEMPLATE_ERROR = "/tools/error";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleError(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp) {
        String statusCode = this.extractStatusCode(req.getRequestURI());
        if (TEMPLATE_ERROR.equals(req.getRequestURI())) {
            ErrorPageUtil.renderGenericErrorPage(req, resp);
        } else if (STATUS_500.equals(statusCode)) {
            ErrorPageUtil.render500Page(req, resp);
        } else if (Requests.hasException(req) && STATUS_500.equals(statusCode)) {
            ErrorPageUtil.render500PageWithExceptionTrace(req, resp);
        } else if (Configs.DEFAULT.undertow().getStringList(KEY_STATUS_CODES).contains(statusCode)) {
            ErrorPageUtil.renderErrorPageForStatusCode(req, resp, statusCode);
        } else {
            ErrorPageUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String extractStatusCode(String requestURI) {
        return requestURI.substring(requestURI.lastIndexOf('/') + 1);
    }
}