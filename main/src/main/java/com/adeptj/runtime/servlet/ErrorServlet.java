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

import com.adeptj.runtime.html.TemplateEngine;
import com.adeptj.runtime.html.TemplateEngineContext;
import com.adeptj.runtime.kernel.util.Environment;
import com.adeptj.runtime.kernel.util.RequestUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serial;

import static com.adeptj.runtime.common.Constants.ERROR_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.VAR_ERROR_CODE;
import static jakarta.servlet.DispatcherType.ERROR;

/**
 * ErrorServlet that serves the error page w.r.t error coded(401, 403, 404, 500).
 * <p>
 * This servlet is invoked by the container using the servlet error page mechanism therefore should not be called directly.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ ErrorServlet", urlPatterns = ERROR_SERVLET_URI)
public class ErrorServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 3178276466531152040L;

    private static final String KEY_EXCEPTION = "exception";

    private static final String ERROR_TEMPLATE = "error";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        // Make sure the below code is invoked only on an error dispatch.
        if (req.getDispatcherType() == ERROR) {
            TemplateEngineContext.Builder builder = TemplateEngineContext.builder(ERROR_TEMPLATE, req, resp)
                    .addTemplateVariable(VAR_ERROR_CODE, resp.getStatus());
            if (Environment.isDev() && RequestUtil.hasException(req)) {
                builder.addTemplateVariable(KEY_EXCEPTION, RequestUtil.getException(req));
            }
            TemplateEngine.getInstance().render(builder.build());
        }
    }
}