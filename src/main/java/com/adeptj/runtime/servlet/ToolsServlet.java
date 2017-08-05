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

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngine;
import org.osgi.framework.Bundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.adeptj.runtime.common.Constants.TOOLS_DASHBOARD_URI;

/**
 * ToolsServlet renders the admin tools page.
 * <p>
 * Note: This is independent of OSGi and directly managed by UndertowServer.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ ToolsServlet", urlPatterns = {"/tools/dashboard"}, asyncSupported = true)
public class ToolsServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    private static final String TOOLS_TEMPLATE = "auth/tools";

    /**
     * Renders tools page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ContextObject ctxObj = new ContextObject();
        Bundle[] bundles = BundleContextHolder.INSTANCE.getBundleContext().getBundles();
        ctxObj.put("username", req.getRemoteUser())
                .put("sysProps", System.getProperties().entrySet())
                .put("totalBundles", bundles.length)
                .put("bundles", bundles)
                .put("runtime", new StringBuilder(System.getProperty("java.runtime.name"))
                        .append("(build ")
                        .append(System.getProperty("java.runtime.version"))
                        .append(")").toString())
                .put("jvm", new StringBuilder(System.getProperty("java.vm.name"))
                        .append("(build ").append(System.getProperty("java.vm.version"))
                        .append(", ")
                        .append(System.getProperty("java.vm.info"))
                        .append(")").toString())
                .put("processors", Runtime.getRuntime().availableProcessors());
        TemplateEngine.defaultEngine().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .template(TOOLS_TEMPLATE)
                .locale(req.getLocale())
                .contextObject(ctxObj)
                .build());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(resp.encodeRedirectURL(TOOLS_DASHBOARD_URI));
    }
}