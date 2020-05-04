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

package com.adeptj.runtime.server;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.servlet.handlers.security.ServletFormAuthenticationMechanism;

import java.util.Map;

/**
 * Customizes the default container authentication url(/j_security_check).
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class ExtendedServletFormAuthenticationMechanismFactory implements AuthenticationMechanismFactory {

    private static final String ADMIN_J_SECURITY_CHECK = "/admin_j_security_check";

    @Override
    public AuthenticationMechanism create(String mechanismName, IdentityManager identityManager,
                                          FormParserFactory formParserFactory, Map<String, String> properties) {
        return new ServletFormAuthenticationMechanism(formParserFactory, mechanismName, properties.get(LOGIN_PAGE),
                properties.get(ERROR_PAGE), ADMIN_J_SECURITY_CHECK);
    }
}
