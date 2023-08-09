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
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;

/**
 * Simple IdentityManager implementation that does the authentication from provisioning file or from
 * the OsgiManager.config file if it is created when password is updated from OSGi Web Console.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class SimpleIdentityManager implements IdentityManager {

    private final UserManager userManager;

    public SimpleIdentityManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * In our case, this method is called by CachedAuthenticatedSessionMechanism.
     * <p>
     * This is queried on each request for protected resources after user is successfully logged in.
     */
    @Override
    public Account verify(Account account) {
        List<String> roles = this.userManager.getRoles(account.getPrincipal().getName());
        boolean match = account.getRoles().stream().anyMatch(roles::contains);
        return match ? account : null;
    }

    /**
     * Called by FormAuthenticationMechanism when user submits the login form.
     */
    @Override
    public Account verify(String id, Credential credential) {
        PasswordCredential pwdCredential = (PasswordCredential) credential;
        String password = this.userManager.getPassword(id);
        if (StringUtils.isEmpty(password)) {
            // This is just called to waste a bit of time as not to reveal that the user does not exist.
            this.userManager.encodePassword(new String(pwdCredential.getPassword()));
            return null;
        }
        SimpleAccount account = null;
        if (this.userManager.matchPassword(pwdCredential.getPassword(), password.toCharArray())) {
            account = new SimpleAccount(new SimplePrincipal(id), new HashSet<>(this.userManager.getRoles(id)));
        }
        return account;
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
