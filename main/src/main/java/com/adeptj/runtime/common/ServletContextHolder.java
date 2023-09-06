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

import jakarta.servlet.ServletContext;

/**
 * This Enum provides the access to the {@link ServletContext} and corresponding attributes.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServletContextHolder {

    INSTANCE;

    private ServletContext context;

    public void setServletContext(ServletContext context) { // NOSONAR
        if (this.context == null) {
            this.context = context;
        }
    }

    public ServletContext getServletContext() {
        return this.context;
    }

    public <T> T getContextAttributeOfType(String name, Class<T> type) {
        final Object value = this.context.getAttribute(name);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void setContextAttribute(String name, Object value) {
        this.context.setAttribute(name, value);
    }

    public static ServletContextHolder getInstance() {
        return INSTANCE;
    }
}
