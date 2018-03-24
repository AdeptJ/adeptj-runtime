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

import com.adeptj.runtime.common.BundleContextHolder;
import com.adeptj.runtime.common.ResponseUtil;
import com.adeptj.runtime.tools.ContextObject;
import com.adeptj.runtime.tools.TemplateContext;
import com.adeptj.runtime.tools.TemplateEngines;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.adeptj.runtime.common.Constants.TOOLS_DASHBOARD_URI;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_VERSION;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_INFO;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_VERSION;

/**
 * ToolsServlet renders the admin tools page.
 * <p>
 * Note: This is independent of OSGi and directly managed by UndertowServer.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(
        name = "AdeptJ ToolsServlet",
        urlPatterns = {
                TOOLS_DASHBOARD_URI
        },
        asyncSupported = true
)
public class ToolsServlet extends HttpServlet {

    private static final long serialVersionUID = -3339904764769823449L;

    private static final String TOOLS_TEMPLATE = "auth/tools";

    /**
     * Renders tools page.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Bundle[] bundles = BundleContextHolder.INSTANCE.getBundleContext().getBundles();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        TemplateEngines.getDefault().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .template(TOOLS_TEMPLATE)
                .locale(req.getLocale())
                .contextObject(ContextObject.newContextObject()
                        .put("username", req.getRemoteUser())
                        .put("sysProps", System.getProperties().entrySet())
                        .put("totalBundles", bundles.length)
                        .put("bundles", bundles)
                        .put("runtime", JAVA_RUNTIME_NAME + "(build " + JAVA_RUNTIME_VERSION + ")")
                        .put("jvm", JAVA_VM_NAME + "(build " + JAVA_VM_VERSION + ", " + JAVA_VM_INFO + ")")
                        .put("startTime", new Date(runtimeMXBean.getStartTime()))
                        .put("upTime", TimeUnit.MILLISECONDS.toMinutes(runtimeMXBean.getUptime()))
                        .put("maxMemory", FileUtils.byteCountToDisplaySize(memoryMXBean.getHeapMemoryUsage().getMax()))
                        .put("usedMemory", FileUtils.byteCountToDisplaySize(memoryMXBean.getHeapMemoryUsage().getUsed()))
                        .put("processors", Runtime.getRuntime().availableProcessors()))
                .build());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResponseUtil.redirect(resp, TOOLS_DASHBOARD_URI);
    }
}