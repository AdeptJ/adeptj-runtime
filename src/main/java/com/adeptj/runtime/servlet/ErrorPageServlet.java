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

import com.adeptj.runtime.common.RequestUtil;
import com.adeptj.runtime.common.ResponseUtil;
import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.templating.TemplateData;
import com.adeptj.runtime.templating.TemplateEngine;
import com.adeptj.runtime.templating.TemplateEngineContext;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adeptj.runtime.common.Constants.SLASH;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * ErrorPageServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.) for admin related operations.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ ErrorPageServlet", urlPatterns = "/error/*", asyncSupported = true)
public class ErrorPageServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    private static final String STATUS_500 = "500";

    private static final String ERROR_URI = "/error";

    private static final String KEY_EXCEPTION = "exception";

    private static final String TEMPLATE_500 = "error/500";

    private static final String TEMPLATE_GENERIC = "error/generic";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    private static final String TEMPLATE_ERROR_FMT = "error/%s";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        String statusCode = StringUtils.substringAfterLast(req.getRequestURI(), SLASH);
        if (StringUtils.equals(ERROR_URI, req.getRequestURI())) {
            this.renderGenericErrorPage(req, resp);
        } else if (StringUtils.equals(STATUS_500, statusCode)) {
            if (RequestUtil.hasException(req)) {
                this.render500PageWithExceptionTrace(req, resp);
            } else {
                this.render500Page(req, resp);
            }
        } else if (Configs.of().undertow().getStringList(KEY_STATUS_CODES).contains(statusCode)) {
            this.renderErrorPageForStatusCode(req, resp, statusCode);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderGenericErrorPage(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateEngineContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_GENERIC)
                .build());
    }

    private void renderErrorPageForStatusCode(HttpServletRequest req, HttpServletResponse resp, String statusCode) {
        TemplateEngine.getInstance().render(TemplateEngineContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(String.format(TEMPLATE_ERROR_FMT, statusCode))
                .build());
    }

    private void render500Page(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateEngineContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .build());
    }

    private void render500PageWithExceptionTrace(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateEngineContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .templateData(new TemplateData().with(KEY_EXCEPTION, RequestUtil.getAttribute(req, ERROR_EXCEPTION)))
                .build());
    }
}