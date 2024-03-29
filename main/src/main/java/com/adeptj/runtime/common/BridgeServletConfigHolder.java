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

package com.adeptj.runtime.common;

import jakarta.servlet.ServletConfig;

/**
 * Maintains the BridgeServlet's {@link ServletConfig} instance.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum BridgeServletConfigHolder {

    INSTANCE;

    private ServletConfig bridgeServletConfig;

    public void setBridgeServletConfig(ServletConfig bridgeServletConfig) { // NOSONAR
        if (this.bridgeServletConfig == null) {
            this.bridgeServletConfig = bridgeServletConfig;
        }
    }

    public ServletConfig getBridgeServletConfig() {
        return bridgeServletConfig;
    }

    public static BridgeServletConfigHolder getInstance() {
        return INSTANCE;
    }
}
