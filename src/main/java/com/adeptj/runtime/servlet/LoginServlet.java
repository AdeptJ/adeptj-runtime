/*
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
package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.Constants;
import com.adeptj.runtime.templating.ContextObject;
import com.adeptj.runtime.templating.TemplateContext;
import com.adeptj.runtime.templating.TemplateEngine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.adeptj.runtime.common.Constants.TOOLS_LOGIN_URI;
import static com.adeptj.runtime.common.Constants.TOOLS_LOGOUT_URI;

/**
 * LoginServlet does the following:
 * <p>
 * 1. Serves the login page and handles the validation failure on wrong credentials submission.
 * 2. Logout the currently logged in Admin user and renders the login page again.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ LoginServlet", urlPatterns = { TOOLS_LOGIN_URI, TOOLS_LOGOUT_URI })
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    /**
     * Render login page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        if (TOOLS_LOGIN_URI.equals(requestURI)) {
        	TemplateEngine.instance().render(new TemplateContext.Builder(req, resp).template("auth/login").build());
        } else if (TOOLS_LOGOUT_URI.equals(requestURI) && req.isUserInRole(Constants.OSGI_ADMIN_ROLE)) {
            // Invalidate the session and redirect to /tools/login page.
            req.logout();
            resp.sendRedirect(Constants.TOOLS_DASHBOARD_URI);
        } else {
            // if someone requesting logout URI anonymously, which doesn't make sense. Redirect to /system/console.
            resp.sendRedirect(Constants.TOOLS_DASHBOARD_URI);
        }
    }

    /**
     * Handle "/auth/j_security_check" validation failure.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.handleLoginFailure(req, resp);
    }

    private void handleLoginFailure(HttpServletRequest req, HttpServletResponse resp) {
        TemplateContext.Builder builder = new TemplateContext.Builder(req, resp);
        ContextObject ctxObj = new ContextObject();
        ctxObj.put("error", "Invalid credentials!!").put("j_username", req.getParameter("j_username"));
        // Render login page again with validation message.
        TemplateEngine.instance().render(builder.template("auth/login").contextObject(ctxObj).build());
    }
}