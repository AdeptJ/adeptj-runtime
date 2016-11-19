/** 
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
package com.adeptj.runtime.undertow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.adeptj.runtime.common.CredentialMatcher;
import com.typesafe.config.Config;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

/**
 * Simple IdentityManager implementation that does the authentication from provisioning file or from the OsgiManager.config
 * file if it is created when password is updated from OSGi Web Console.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class FileIdentityManager implements IdentityManager {

	private static final String KEY_USER_ROLES_MAPPING = "common.user-roles-mapping";
	
	/**
	 * User to Roles mapping.
	 */
	private Map<String, List<String>> userRolesMapping;

	@SuppressWarnings("unchecked")
	public FileIdentityManager(Config undertowCfg) {
		this.userRolesMapping = new HashMap<>(Map.class.cast(undertowCfg.getObject(KEY_USER_ROLES_MAPPING).unwrapped()));
	}

	/**
	 * In our case, this method is called by CachedAuthenticatedSessionMechanism.
	 * 
	 * This is queried on each request after user is successfully logged in.
	 */
	@Override
	public Account verify(Account account) {
		return this.userRolesMapping.entrySet().stream().filter(entry -> entry.getKey().equals(account.getPrincipal().getName()))
				.anyMatch(entry -> entry.getValue().containsAll(account.getRoles())) ? account : null;
	}

	/**
	 * Called by FormAuthenticationMechanism when user submits the login form.
	 */
	@Override
	public Account verify(String id, Credential credential) {
		return this.userRolesMapping.entrySet().stream().filter(entry -> entry.getKey().equals(id))
				.map(entry -> CredentialMatcher.INSTANCE.matches(id, new String(((PasswordCredential) credential).getPassword()))
						? new SimpleAccount(new SimplePrincipal(id), new HashSet<>(this.userRolesMapping.get(id)))
						: null).filter(Objects::nonNull).findFirst().get();
	}

	/**
	 * Used here:
	 * 
	 * 1. ClientCertAuthenticationMechanism.
	 * 2. GSSAPIAuthenticationMechanism
	 * 
	 * We are not covering both the use cases therefore returning a null.
	 */
	@Override
	public Account verify(Credential credential) {
		return null;
	}
}
