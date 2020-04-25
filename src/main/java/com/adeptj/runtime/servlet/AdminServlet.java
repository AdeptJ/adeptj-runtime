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
import com.adeptj.runtime.templating.TemplateData;
import com.adeptj.runtime.templating.TemplateEngine;
import com.adeptj.runtime.templating.TemplateEngineContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adeptj.runtime.common.Constants.ADMIN_LOGIN_URI;
import static com.adeptj.runtime.common.Constants.ADMIN_LOGOUT_URI;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.DEFAULT_LANDING_PAGE_URI;
import static com.adeptj.runtime.common.Constants.OSGI_ADMIN_ROLE;
import static java.lang.Boolean.TRUE;

/**
 * AdminServlet does the following:
 * <p>
 * 1. Renders the login page and handles the validation failure on wrong credentials submission.
 * 2. Logout the currently logged in Admin user and renders the login page again.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ AdminServlet", urlPatterns = ADMIN_SERVLET_URI, asyncSupported = true)
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    private static final String LOGIN_TEMPLATE = "auth/login";

    private static final String J_USERNAME = "j_username";

    private static final String IS_LOGIN_ERROR = "isLoginError";

    /**
     * Render login page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (ADMIN_LOGIN_URI.equals(req.getRequestURI())) {
            TemplateEngine.getInstance().render(TemplateEngineContext.builder(LOGIN_TEMPLATE, resp).build());
            return;
        }
        if (ADMIN_LOGOUT_URI.equals(req.getRequestURI()) && req.isUserInRole(OSGI_ADMIN_ROLE)) {
            // Invalidate the session.
            RequestUtil.logout(req);
        }
        // if someone requesting logout URI anonymously, which doesn't make sense. Redirect to /system/console/bundles.
        ResponseUtil.redirect(resp, DEFAULT_LANDING_PAGE_URI);
    }

    /**
     * Handle "/auth/j_security_check" validation failure.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Render login page again with validation message.
        TemplateEngine.getInstance()
                .render(TemplateEngineContext.builder(LOGIN_TEMPLATE, resp)
                        .templateData(new TemplateData()
                                .with(IS_LOGIN_ERROR, TRUE)
                                .with(J_USERNAME, req.getParameter(J_USERNAME)))
                        .build());
    }
}