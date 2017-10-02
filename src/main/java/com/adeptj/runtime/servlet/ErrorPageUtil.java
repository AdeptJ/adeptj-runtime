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
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.RenderException;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * ErrorPageUtil
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ErrorPageUtil {

    private static final String STATUS_500 = "500";

    private static final String TEMPLATE_ERROR = "/tools/error";

    private static final String KEY_EXCEPTION = "exception";

    private static final String TEMPLATE_500 = "error/500";

    private static final String TEMPLATE_GENERIC = "error/generic";

    private static final String KEY_STATUS_CODES = "common.status-codes";

    private static final String TEMPLATE_ERROR_RESOLVABLE = "error/%s";

    private ErrorPageUtil() {
    }

    public static void renderGenericErrorPage(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_GENERIC)
                .build());
    }

    public static void renderErrorPageForStatusCode(HttpServletRequest req, HttpServletResponse resp, String statusCode) {
        TemplateEngine.getInstance().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(String.format(TEMPLATE_ERROR_RESOLVABLE, statusCode))
                .build());
    }

    public static void render500Page(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .build());
    }

    public static void render500PageWithExceptionTrace(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngine.getInstance().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .locale(req.getLocale())
                .template(TEMPLATE_500)
                .contextObject(ContextObject.newContextObject()
                        .put(KEY_EXCEPTION, Requests.attr(req, ERROR_EXCEPTION)))
                .build());
    }

    public static void sendError(HttpServletResponse resp, int errorCode) {
        try {
            resp.sendError(errorCode);
        } catch (IOException ex) {
            // Now what? may be log and re-throw. Let container handle it.
            throw new RenderException(ex.getMessage(), ex);
        }
    }

}
