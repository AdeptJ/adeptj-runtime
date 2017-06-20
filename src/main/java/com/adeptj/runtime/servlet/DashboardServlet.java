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
import com.adeptj.runtime.templating.ContextObject;
import com.adeptj.runtime.templating.TemplateContext;
import com.adeptj.runtime.templating.TemplateEngine;
import org.osgi.framework.Bundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DashboardServlet renders the admin dashboard page.
 * <p>
 * Note: This is independent of OSGi and directly managed by UndertowServer.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "DashboardServlet", urlPatterns = { "/tools/dashboard" })
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    /**
     * Render dashboard page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	ContextObject ctxObj = new ContextObject();
    	ctxObj.put("username", req.getRemoteUser()).put("sysProps", System.getProperties().entrySet());
    	Bundle[] bundles = BundleContextHolder.INSTANCE.getBundleContext().getBundles();
    	ctxObj.put("totalBundles", bundles.length).put("bundles", bundles);
    	StringBuilder jreInfo = new StringBuilder(System.getProperty("java.runtime.name"));
    	jreInfo.append("(build ").append(System.getProperty("java.runtime.version")).append(")");
    	ctxObj.put("runtime", jreInfo.toString());
    	StringBuilder jvmInfo = new StringBuilder(System.getProperty("java.vm.name")).append("(build ");
    	jvmInfo.append(System.getProperty("java.vm.version")).append(", ").append(System.getProperty("java.vm.info")).append(")");
    	ctxObj.put("jvm", jvmInfo.toString()).put("processors", Runtime.getRuntime().availableProcessors());
        TemplateEngine.instance().render(new TemplateContext.Builder(req, resp).contextObject(ctxObj).template("auth/tools").build());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/tools");
    }
}