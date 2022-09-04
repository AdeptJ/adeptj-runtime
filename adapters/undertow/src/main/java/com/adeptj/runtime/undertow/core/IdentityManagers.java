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

package com.adeptj.runtime.undertow.core;

import com.adeptj.runtime.kernel.UserManager;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.PasswordCredential;
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
     * @param userManager the {@link UserManager} for getting user passwords, roles etc.
     * @return an Account composed with the Principal and associated roles.
     */
    static Account verifyCredentials(Map<String, Set<String>> rolesByUser,
                                     String id, PasswordCredential credential, UserManager userManager) {
        String password = userManager.getPassword(id);
        if (StringUtils.isEmpty(password)) {
            // This is just called to waste a bit of time as not to reveal that the user does not exist.
            userManager.encodePassword(new String(credential.getPassword()));
            return null;
        }
        boolean pwdMatch = userManager.match(credential.getPassword(), password.toCharArray());
        return rolesByUser.entrySet()
                .stream()
                .filter(entry -> StringUtils.equals(entry.getKey(), id) && pwdMatch)
                .map(entry -> new SimpleAccount(new SimplePrincipal(entry.getKey()), entry.getValue()))
                .findFirst()
                .orElse(null);
    }
}
