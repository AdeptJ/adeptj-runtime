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
package com.adeptj.runtime.initializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * StartupHandler that will be called by the ServletContainerInitializer while startup is in progress.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public interface StartupHandler {

    /**
     * This method will be called by the StartupHandlerInitializer while startup is in
     * progress.
     *
     * @param context the {@link ServletContext} in which this handler runs in.
     * @throws ServletException exception thrown by initializer code
     */
    void onStartup(ServletContext context) throws ServletException;
}
