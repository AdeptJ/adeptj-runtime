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

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Collections;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.EXTENSION_DIRECTIVE;
import static org.osgi.framework.Constants.FRAGMENT_HOST;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;

/**
 * Utility for creating OSGi Filter for tracking/finding Services etc.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class OSGiUtil {

    private static final String FILTER_AND = "(&(";

    private static final String EQ = "=";

    private static final String PARENTHESIS_CLOSE = ")";

    // No instantiation. Utility methods only.
    private OSGiUtil() {
    }

    public static boolean isNotFragment(Bundle bundle) {
        return bundle.getHeaders().get(FRAGMENT_HOST) == null;
    }

    public static boolean isSystemBundleFragment(Bundle bundle) {
        return StringUtils.contains(bundle.getHeaders().get(FRAGMENT_HOST), EXTENSION_DIRECTIVE);
    }

    public static boolean isNotBundle(Manifest manifest) {
        return manifest != null && StringUtils.isEmpty(manifest.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME));
    }

    public static Filter filter(BundleContext context, Class<?> objectClass, String filterExpr) {
        try {
            return context.createFilter(FILTER_AND +
                    OBJECTCLASS +
                    EQ +
                    objectClass.getName() +
                    PARENTHESIS_CLOSE +
                    filterExpr +
                    PARENTHESIS_CLOSE);
        } catch (InvalidSyntaxException ex) {
            // Filter expression is malformed, not RFC-1960 based Filter.
            throw new IllegalArgumentException(ex);
        }
    }

    public static String getServiceDesc(ServiceReference<?> reference) {
        return getString(reference, SERVICE_DESCRIPTION);
    }

    public static String getString(ServiceReference<?> reference, String key) {
        return (String) reference.getProperty(key);
    }

    public static Set<String> arrayToSet(ServiceReference<?> reference, String key) {
        // Not doing any type check as the property has to be an array type suggested by method name itself.
        String[] values = (String[]) reference.getProperty(key);
        if (values == null || values.length == 0) {
            return Collections.emptySet();
        }
        return Stream.of(values)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
