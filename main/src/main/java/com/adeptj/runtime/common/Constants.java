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

/**
 * Constants, common constants for AdeptJ Runtime.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Constants {

    public static final String BUNDLES_ROOT_DIR_KEY = "bundles-root-dir";

    public static final String ATTRIBUTE_BUNDLE_CONTEXT = "org.osgi.framework.BundleContext";

    public static final String PEBBLE_CONF_SECTION = "pebble";

    public static final String FELIX_CONF_SECTION = "felix";

    public static final String LOGGING_CONF_SECTION = "logging";

    public static final String ADMIN_LOGIN_URI = "/admin/login";

    public static final String ADMIN_LOGOUT_URI = "/admin/logout";

    public static final String ADMIN_SERVLET_NAME = "AdeptJ AdminServlet";

    public static final String ADMIN_SERVLET_URI = "/admin/*";

    public static final String ERROR_SERVLET_URI = "/ErrorHandler";


    public static final String ERROR_SERVLET_NAME = "AdeptJ ErrorServlet";

    public static final String BANNER_TXT = "/banner.txt";

    public static final String CONTENT_TYPE_HTML_UTF8 = "text/html;charset=UTF-8";

    public static final String OSGI_ADMIN_ROLE = "OSGiAdmin";

    public static final String MV_CREDENTIALS_STORE = "credentials.dat";

    public static final String H2_MAP_ADMIN_CREDENTIALS = "adminCredentials";

    /**
     * Deny direct instantiation.
     */
    private Constants() {
    }
}
