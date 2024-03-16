/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.jetty.handler;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.RequestUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * Jetty handler for context path handling.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class ContextPathHandler extends Handler.Abstract {

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (RequestUtil.isContextRootRequest(request.getHttpURI().getPath())) {
            String systemConsolePath = ConfigProvider.getInstance()
                    .getMainConfig()
                    .getString("common.system-console-path");
            Response.sendRedirect(request, response, callback, systemConsolePath);
            return true;
        }
        return false;
    }
}
