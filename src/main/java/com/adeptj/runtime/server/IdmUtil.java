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
import io.undertow.security.idm.PasswordCredential;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.adeptj.runtime.server.CredentialMatcher.match;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

/**
 * Utility methods for {@link io.undertow.security.idm.IdentityManager}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class IdmUtil {

    private IdmUtil() {
    }

    static Account verifyAccount(Map<String, List<String>> userRolesMapping, Account account) {
        return userRolesMapping
                .entrySet()
                .stream()
                .anyMatch(entry -> StringUtils.equals(entry.getKey(), account.getPrincipal().getName())
                        && entry.getValue().containsAll(account.getRoles()))
                ? account : null;
    }

    static Account verifyCredentials(Map<String, List<String>> userRolesMapping, String id, PasswordCredential credential) {
        return userRolesMapping
                .entrySet()
                .stream()
                .filter(entry -> IdmUtil.verifyCredentials(entry.getKey(), id, credential.getPassword()))
                .map(entry -> new SimpleAccount(new SimplePrincipal(entry.getKey()), new HashSet<>(entry.getValue())))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verify the given credentials.
     *
     * @param username one from configs.
     * @param id       one that is submitted by client.
     * @param password one that is submitted by client.
     * @return boolean to indicate whether the credentials verification was successful or not.
     */
    private static boolean verifyCredentials(String username, String id, char[] password) {
        return StringUtils.equals(username, id) && isNotEmpty(password) && match(username, new String(password));
    }
}
