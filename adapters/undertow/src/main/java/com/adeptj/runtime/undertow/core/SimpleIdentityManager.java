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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;

/**
 * Simple IdentityManager implementation that does the authentication from provisioning file or from
 * the OsgiManager.config file if it is created when password is updated from OSGi Web Console.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class SimpleIdentityManager implements IdentityManager {

    private static final String KEY_USER_ROLES_MAPPING = "common.user-roles-mapping";

    private final UserManager userManager;

    /**
     * User to Roles mapping.
     */
    private final Map<String, Set<String>> rolesByUser;

    public SimpleIdentityManager(UserManager userManager, Config cfg) {
        this.userManager = userManager;
        this.rolesByUser = new HashMap<>();
        for (Entry<String, ConfigValue> entry : cfg.getObject(KEY_USER_ROLES_MAPPING).entrySet()) {
            Set<String> roles = new HashSet<>();
            for (ConfigValue role : (ConfigList) entry.getValue()) {
                roles.add((String) role.unwrapped());
            }
            this.rolesByUser.put(entry.getKey(), roles);
        }
    }

    /**
     * In our case, this method is called by CachedAuthenticatedSessionMechanism.
     * <p>
     * This is queried on each request for protected resources after user is successfully logged in.
     */
    @Override
    public Account verify(Account account) {
        return IdentityManagers.verifyAccount(this.rolesByUser, account) ? account : null;
    }

    /**
     * Called by FormAuthenticationMechanism when user submits the login form.
     */
    @Override
    public Account verify(String id, Credential credential) {
        return IdentityManagers.verifyCredentials(this.rolesByUser, id, (PasswordCredential) credential, this.userManager);
    }

    /**
     * Used here:
     * <p>
     * 1. ClientCertAuthenticationMechanism.
     * 2. GSSAPIAuthenticationMechanism
     * <p>
     * We are not covering both the use cases therefore returning a null.
     */
    @Override
    public Account verify(Credential credential) {
        return null;
    }
}
