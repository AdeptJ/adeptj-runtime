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
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngines;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adeptj.runtime.common.Constants.SLASH;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static javax.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * ErrorPages
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ErrorPages {

    private static final String STATUS_500 = "500";

    private static final String TEMPLATE_ERROR = "/tools/error";

    private static final String KEY_STATUS_CODE = "statusCode";

    private static final String KEY_ERROR_MSG = "errorMsg";

    private static final String KEY_REQ_URI = "reqURI";

    private static final String KEY_EXCEPTION = "exception";

    private static final String TEMPLATE_500 = "error/500";

    private static final String TEMPLATE_GENERIC = "error/generic";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    private static final String TEMPLATE_ERROR_RESOLVABLE = "error/%s";

    private ErrorPages() {
    }

    public static void renderOSGiErrorPage(HttpServletRequest req, HttpServletResponse resp) {
        Integer statusCode = (Integer) RequestUtil.getAttribute(req, ERROR_STATUS_CODE);
        if (RequestUtil.hasException(req) && Integer.valueOf(SC_INTERNAL_SERVER_ERROR).equals(statusCode)) {
            TemplateEngines.getDefault().render(TemplateContext.builder()
                    .request(req)
                    .response(resp)
                    .locale(req.getLocale())
                    .contextObject(ContextObject.newContextObject()
                            .put(KEY_STATUS_CODE, statusCode)
                            .put(KEY_ERROR_MSG, RequestUtil.getAttribute(req, ERROR_MESSAGE))
                            .put(KEY_REQ_URI, RequestUtil.getAttribute(req, ERROR_REQUEST_URI))
                            .put(KEY_EXCEPTION, RequestUtil.getAttribute(req, ERROR_EXCEPTION)))
                    .template(TEMPLATE_500)
                    .build());
        } else if (Integer.valueOf(SC_INTERNAL_SERVER_ERROR).equals(statusCode)) {
            ErrorPages.render500Page(req, resp);
        } else if (Configs.DEFAULT.undertow().getIntList(KEY_STATUS_CODES).contains(statusCode)) {
            ErrorPages.renderErrorPageForStatusCode(req, resp, String.valueOf(statusCode));
        }
    }

    static void renderErrorPage(HttpServletRequest req, HttpServletResponse resp) {
        String statusCode = StringUtils.substringAfterLast(req.getRequestURI(), SLASH);
        if (StringUtils.equals(TEMPLATE_ERROR, req.getRequestURI())) {
            ErrorPages.renderGenericErrorPage(req, resp);
        } else if (StringUtils.equals(STATUS_500, statusCode)) {
            ErrorPages.render500Page(req, resp);
        } else if (RequestUtil.hasException(req) && StringUtils.equals(STATUS_500, statusCode)) {
            ErrorPages.render500PageWithExceptionTrace(req, resp);
        } else if (Configs.DEFAULT.undertow().getStringList(KEY_STATUS_CODES).contains(statusCode)) {
            ErrorPages.renderErrorPageForStatusCode(req, resp, statusCode);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static void renderGenericErrorPage(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngines.getDefault().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_GENERIC)
                .build());
    }

    private static void renderErrorPageForStatusCode(HttpServletRequest req, HttpServletResponse resp, String statusCode) {
        TemplateEngines.getDefault().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(String.format(TEMPLATE_ERROR_RESOLVABLE, statusCode))
                .build());
    }

    private static void render500Page(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngines.getDefault().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .build());
    }

    private static void render500PageWithExceptionTrace(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngines.getDefault().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .contextObject(ContextObject.newContextObject()
                        .put(KEY_EXCEPTION, RequestUtil.getAttribute(req, ERROR_EXCEPTION)))
                .build());
    }
}
