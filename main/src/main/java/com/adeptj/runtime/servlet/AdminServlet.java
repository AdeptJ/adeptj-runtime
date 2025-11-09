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
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import com.adeptj.runtime.kernel.util.RequestUtil;
import com.adeptj.runtime.kernel.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.Strings;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.Serial;

import static com.adeptj.runtime.common.Constants.ADMIN_LOGOUT_URI;
import static com.adeptj.runtime.common.Constants.ADMIN_SERVLET_URI;
import static com.adeptj.runtime.common.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.common.Constants.MV_CREDENTIALS_STORE;
import static com.adeptj.runtime.common.Constants.OSGI_ADMIN_ROLE;
import static com.adeptj.runtime.kernel.Constants.ADMIN_LOGIN_URI;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * AdminServlet does the following:
 * <p>
 * 1. Renders the login page and handles the validation failure on wrong credential submission.
 * 2. Logout the currently logged in Admin user and renders the login page again.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ AdminServlet", urlPatterns = ADMIN_SERVLET_URI, asyncSupported = true)
public class AdminServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -8401648641965144307L;

    private static final String LOGIN_TEMPLATE = "login";

    private static final String J_USERNAME = "j_username";

    private static final String LOGIN_ERROR = "loginError";

    private static final String ADMIN_CHANGE_PWD_URI = "/admin/change-pwd";

    private static final String TEMPLATE_CHANGE_PWD = "change-pwd";

    /**
     * Handles the following.
     * 1. change-pwd page rendering
     * 2. login page rendering
     * 3. logout and redirect to system console
     * 4. else just sends a 404 error
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (ADMIN_CHANGE_PWD_URI.equals(req.getRequestURI())) {
            TemplateEngine.getInstance().render(TemplateEngineContext.builder(TEMPLATE_CHANGE_PWD, req, resp).build());
        } else if (ADMIN_LOGIN_URI.equals(req.getRequestURI())) {
            TemplateEngine.getInstance().render(TemplateEngineContext.builder(LOGIN_TEMPLATE, req, resp).build());
        } else if (ADMIN_LOGOUT_URI.equals(req.getRequestURI())) {
            if (req.isUserInRole(OSGI_ADMIN_ROLE)) {
                // Invalidate the session.
                RequestUtil.logout(req);
            }
            ResponseUtil.redirectToSystemConsole(resp);
        } else {
            ResponseUtil.sendError(resp, SC_NOT_FOUND);
        }
    }

    /**
     * Handles the following.
     * 1. change the admin password
     * 2. login page rendering with validation message in case "j_security_check" validation failure.
     * 3. else just sends a 404 error
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (ADMIN_CHANGE_PWD_URI.equals(req.getRequestURI())) {
            this.changePassword(req, resp);
        } else if (ADMIN_LOGIN_URI.equals(req.getRequestURI())) {
            TemplateEngineContext templateEngineContext = TemplateEngineContext.builder(LOGIN_TEMPLATE, req, resp)
                    .addTemplateVariable(LOGIN_ERROR, TRUE)
                    .addTemplateVariable(J_USERNAME, req.getParameter(J_USERNAME))
                    .build();
            TemplateEngine.getInstance().render(templateEngineContext);
        } else {
            ResponseUtil.sendError(resp, SC_NOT_FOUND);
        }
    }

    private void changePassword(HttpServletRequest req, HttpServletResponse resp) {
        TemplateEngineContext templateEngineContext;
        TemplateEngineContext.Builder builder = TemplateEngineContext.builder(TEMPLATE_CHANGE_PWD, req, resp);
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        if (Strings.CS.equals(password, confirmPassword)) {
            try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
                MVMap<String, String> credentials = store.openMap(H2_MAP_ADMIN_CREDENTIALS);
                String encodedPwd = new String(PasswordEncoder.encodePassword(password), UTF_8);
                credentials.put(req.getUserPrincipal().getName(), encodedPwd);
            }
            templateEngineContext = builder.addTemplateVariable("pwdChanged", TRUE).build();
        } else {
            templateEngineContext = builder.addTemplateVariable("pwdMismatch", TRUE).build();
        }
        TemplateEngine.getInstance().render(templateEngineContext);
    }
}