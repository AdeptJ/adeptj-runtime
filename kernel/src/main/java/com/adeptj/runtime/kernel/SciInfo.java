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
package com.adeptj.runtime.kernel;

import jakarta.servlet.ServletContainerInitializer;
import java.util.Set;

public class SciInfo {

    private ServletContainerInitializer sci;

    private final Set<Class<?>> handleTypes;

    private Class<? extends ServletContainerInitializer> sciClass;

    // Tomcat and Jetty expects an instance of ServletContainerInitializer.
    public SciInfo(ServletContainerInitializer sci, Set<Class<?>> handleTypes) {
        this.sci = sci;
        this.handleTypes = handleTypes;
    }

    // Undertow expects ServletContainerInitializer class.
    public SciInfo(Class<? extends ServletContainerInitializer> sciClass, Set<Class<?>> handleTypes) {
        this.sciClass = sciClass;
        this.handleTypes = handleTypes;
    }

    public ServletContainerInitializer getSciInstance() {
        return sci;
    }

    public Class<? extends ServletContainerInitializer> getSciClass() {
        return sciClass;
    }

    public Set<Class<?>> getHandleTypes() {
        return handleTypes;
    }

    public Class<?>[] getHandleTypesArray() {
        return this.handleTypes.toArray(new Class[0]);
    }
}
