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

package com.adeptj.runtime.servlet.osgi;

import com.adeptj.runtime.common.Requests;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.servlet.ErrorPageUtil;
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static javax.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * PerServletContextErrorServlet handles the error codes and exceptions for each ServletContext registered with OSGi.
 * <p><b>
 * Note: This is independent of UndertowServer and directly managed by OSGi.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ PerServletContextErrorServlet", asyncSupported = true)
public class PerServletContextErrorServlet extends HttpServlet {

    private static final long serialVersionUID = -5818850813832379842L;

    private static final String KEY_STATUS_CODE = "statusCode";

    private static final String KEY_ERROR_MSG = "errorMsg";

    private static final String KEY_REQ_URI = "reqURI";

    private static final String KEY_EXCEPTION = "exception";

    private static final String TEMPLATE_404 = "error/404";

    private static final String TEMPLATE_500 = "error/500";

    private static final String TEMPLATE_GENERIC = "error/generic";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    private static final String TEMPLATE_ERROR_RESOLVABLE = "error/%s";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleError(req, resp);
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp) {
        Integer statusCode = (Integer) Requests.attr(req, ERROR_STATUS_CODE);
        TemplateContext.Builder builder = TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .contextObject(this.contextObject(req, statusCode));
        if (Requests.hasException(req) && Integer.valueOf(SC_INTERNAL_SERVER_ERROR).equals(statusCode)) {
            TemplateEngine.getInstance().render(builder.template(TEMPLATE_500).build());
        } else if (Integer.valueOf(SC_INTERNAL_SERVER_ERROR).equals(statusCode)) {
            ErrorPageUtil.renderGenericErrorPage(req, resp);
        } else if (Configs.DEFAULT.undertow().getIntList(KEY_STATUS_CODES).contains(statusCode)) {
            ErrorPageUtil.renderErrorPageForStatusCode(req, resp, String.valueOf(statusCode));
        } else {
            // if the requested view not found, render 404.
            TemplateEngine.getInstance().render(builder.template(TEMPLATE_404).build());
        }
    }

    private ContextObject contextObject(HttpServletRequest req, Integer statusCode) {
        return ContextObject.newContextObject()
                .put(KEY_STATUS_CODE, statusCode)
                .put(KEY_ERROR_MSG, Requests.attr(req, ERROR_MESSAGE))
                .put(KEY_REQ_URI, Requests.attr(req, ERROR_REQUEST_URI))
                .put(KEY_EXCEPTION, Requests.attr(req, ERROR_EXCEPTION));
    }
}
