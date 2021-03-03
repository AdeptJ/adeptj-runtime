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
package com.adeptj.runtime.osgi;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * {@link ServletConfig} for Felix {@link org.apache.felix.http.base.internal.dispatch.DispatcherServlet}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DispatcherServletConfig implements ServletConfig {

    private final ServletConfig bridgeServletConfig;

    public DispatcherServletConfig(ServletConfig bridgeServletConfig) {
        this.bridgeServletConfig = bridgeServletConfig;
    }

    @Override
    public String getServletName() {
        return "Felix DispatcherServlet in Bridge Mode";
    }

    @Override
    public ServletContext getServletContext() {
        return this.bridgeServletConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        return this.bridgeServletConfig.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return this.bridgeServletConfig.getInitParameterNames();
    }
}
