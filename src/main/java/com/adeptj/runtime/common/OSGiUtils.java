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
package com.adeptj.runtime.common;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;

import java.util.Optional;

/**
 * Utility for creating OSGi Filter for tracking/finding Services etc.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class OSGiUtils {

    // No instantiation. Utility methods only.
    private OSGiUtils() {
    }

    public static Filter filter(BundleContext context, Class<?> objectClass, String filterExpr) {
        try {
            StringBuilder filterExprBuilder = new StringBuilder();
            filterExprBuilder.append("(&(").append(Constants.OBJECTCLASS).append("=");
            filterExprBuilder.append(objectClass.getName()).append(")");
            filterExprBuilder.append(filterExpr).append(")");
            return context.createFilter(filterExprBuilder.toString());
        } catch (InvalidSyntaxException ex) {
            // Filter expression is malformed, not RFC-1960 based Filter.
            throw new IllegalArgumentException("InvalidSyntaxException!!", ex);
        }
    }

    public static void unregisterService(ServiceRegistration<?> registration) {
        Optional.ofNullable(registration).ifPresent(ServiceRegistration::unregister);
    }
}
