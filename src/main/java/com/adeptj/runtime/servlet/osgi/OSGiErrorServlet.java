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

package com.adeptj.runtime.servlet.osgi;

import com.adeptj.runtime.servlet.ErrorPageRenderer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OSGiErrorServlet handles the error codes and exceptions for each ServletContext registered with OSGi.
 * <p><b>
 * Note: This is independent of Undertow and directly managed by OSGi.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ OSGiErrorServlet", asyncSupported = true)
public class OSGiErrorServlet extends HttpServlet {

    private static final long serialVersionUID = -5818850813832379842L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        ErrorPageRenderer.renderOSGiErrorPage(req, resp);
    }
}
