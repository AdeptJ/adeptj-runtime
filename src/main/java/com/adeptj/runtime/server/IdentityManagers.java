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

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Utility methods for {@link io.undertow.security.idm.IdentityManager}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class IdentityManagers {

    private IdentityManagers() {
    }

    static boolean verifyAccount(Map<String, Set<String>> rolesByUser, Account account) {
        return rolesByUser.entrySet()
                .stream()
                .anyMatch(entry -> StringUtils.equals(entry.getKey(), account.getPrincipal().getName())
                        && entry.getValue().containsAll(account.getRoles()));
    }

    /**
     * Verify the given credentials.
     *
     * @param rolesByUser the user to roles mapping from configs.
     * @param id          one that is submitted by client.
     * @param credential  the submitted user credential.
     * @return an Account composed with the Principal and associated roles.
     */
    static Account verifyCredentials(Map<String, Set<String>> rolesByUser, String id, Credential credential) {
        return rolesByUser.entrySet()
                .stream()
                .filter(entry -> StringUtils.equals(entry.getKey(), id) && CredentialMatcher.match(entry.getKey(), credential))
                .map(entry -> new SimpleAccount(new SimplePrincipal(entry.getKey()), entry.getValue()))
                .findFirst()
                .orElse(null);
    }
}
