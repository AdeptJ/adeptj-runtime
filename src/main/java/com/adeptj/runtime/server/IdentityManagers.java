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

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for {@link io.undertow.security.idm.IdentityManager}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class IdentityManagers {

    private IdentityManagers() {
    }

    static boolean verifyAccount(Map<String, List<String>> userRolesMapping, Account account) {
        return userRolesMapping.entrySet()
                .stream()
                .anyMatch(entry -> StringUtils.equals(entry.getKey(), account.getPrincipal().getName())
                        && entry.getValue().containsAll(account.getRoles()));
    }

    /**
     * Verify the given credentials.
     *
     * @param userRolesMapping the user to role mapping from configs.
     * @param id               one that is submitted by client.
     * @param credential       the submitted user credential.
     * @return boolean to indicate whether the credentials verification was successful or not.
     */
    static Account verifyCredentials(Map<String, List<String>> userRolesMapping, String id, Credential credential) {
        return userRolesMapping.entrySet()
                .stream()
                .filter(entry -> StringUtils.equals(entry.getKey(), id) && CredentialMatcher.match(entry.getKey(), credential))
                .map(entry -> new SimpleAccount(new SimplePrincipal(entry.getKey()), new HashSet<>(entry.getValue())))
                .findFirst()
                .orElse(null);
    }
}
