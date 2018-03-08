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

import com.adeptj.runtime.common.StartupOrder;
import com.adeptj.runtime.core.StartupAware;

import javax.servlet.ServletContext;

/**
 * FrameworkLauncher is a {@link com.adeptj.runtime.core.StartupAware} that launches the OSGi Framework.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@StartupOrder(0)
public class FrameworkLauncher implements StartupAware {

    /**
     * This method will be called by the ServletContainerInitializer while startup is in progress.
     *
     * @param context the {@link ServletContext}
     */
    @Override
    public void onStartup(ServletContext context) {
        FrameworkManager.INSTANCE.startFramework(context);
    }
}
