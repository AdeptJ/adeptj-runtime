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
import com.adeptj.runtime.common.Constants;
import com.adeptj.runtime.common.Times;
import com.adeptj.runtime.templating.TemplateContext;
import com.adeptj.runtime.templating.TemplateData;
import com.adeptj.runtime.templating.TemplateEngine;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_RUNTIME_VERSION;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_INFO;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_NAME;
import static org.apache.commons.lang3.SystemUtils.JAVA_VM_VERSION;

/**
 * ToolsServlet renders the admin tools page.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(
        name = "AdeptJ ToolsServlet",
        urlPatterns = {
                Constants.TOOLS_DASHBOARD_URI
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
        Bundle[] bundles = BundleContextHolder.getInstance().getBundleContext().getBundles();
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        TemplateEngine.getInstance().render(TemplateContext.builder()
                .request(req)
                .response(resp)
                .template(TOOLS_TEMPLATE)
                .locale(req.getLocale())
                .templateData(TemplateData.newTemplateData()
                        .with("username", req.getRemoteUser())
                        .with("sysProps", System.getProperties().entrySet())
                        .with("totalBundles", bundles.length)
                        .with("bundles", bundles)
                        .with("runtime", JAVA_RUNTIME_NAME + "(build " + JAVA_RUNTIME_VERSION + ")")
                        .with("jvm", JAVA_VM_NAME + "(build " + JAVA_VM_VERSION + ", " + JAVA_VM_INFO + ")")
                        .with("startTime", Date.from(Instant.ofEpochMilli(startTime)))
                        .with("upTime", Times.format(startTime))
                        .with("maxMemory", FileUtils.byteCountToDisplaySize(memoryUsage.getMax()))
                        .with("usedMemory", FileUtils.byteCountToDisplaySize(memoryUsage.getUsed()))
                        .with("processors", Runtime.getRuntime().availableProcessors()))
                .build());
    }
}