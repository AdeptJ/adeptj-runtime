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
package com.adeptj.modularweb.runtime.undertow;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.adeptj.modularweb.runtime.common.OSGiConsolePasswords;
import com.typesafe.config.Config;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

/**
 * OSGiConsoleIdentityManager.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsoleIdentityManager implements IdentityManager {

	private Map<String, List<String>> principalRoleMapping;

	@SuppressWarnings("unchecked")
	public OSGiConsoleIdentityManager(Config undertowConfig) {
		this.principalRoleMapping = new HashMap<>();
		undertowConfig.getObject("common.osgi-console-user-role-mapping").unwrapped().forEach((principal, roles) -> {
			this.principalRoleMapping.put(principal, (List<String>) roles);
		});
	}

	/**
	 * In our case, this method is called by CachedAuthenticatedSessionMechanism.
	 * 
	 * This is queried on each request after user is successfully logged in.
	 */
	@Override
	public Account verify(Account account) {
		if (account != null) {
			String id = account.getPrincipal().getName();
			if (this.principalRoleMapping.containsKey(id)) {
				if (this.principalRoleMapping.get(id).containsAll(account.getRoles())) {
					return account;
				}
			}
		}
		return null;
	}

	/**
	 * Called by FormAuthenticationMechanism when user submits the login form.
	 */
	@Override
	public Account verify(String id, Credential credential) {
		PasswordCredential pwdCredential = (PasswordCredential) credential;
		return OSGiConsolePasswords.INSTANCE.matches(id, new String(pwdCredential.getPassword()))
				? new OSGiConsoleAccount(new OSGiConsolePrincipal(id, pwdCredential), this.principalRoleMapping
						.getOrDefault(id, Collections.emptyList()).stream().collect(Collectors.toSet()))
				: null;
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
