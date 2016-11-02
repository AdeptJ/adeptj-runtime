package com.adeptj.modularweb.runtime.undertow;

import java.security.Principal;
import java.util.Set;

import io.undertow.security.idm.Account;

/**
 * OSGiConsoleAccount.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsoleAccount implements Account {

	private static final long serialVersionUID = -2090504892837494810L;
	
	private OSGiConsolePrincipal principal;
	
	private Set<String> roles;
	
	public OSGiConsoleAccount(OSGiConsolePrincipal principal, Set<String> roles) {
		this.principal = principal;
		this.roles = roles;
	}

	@Override
	public Principal getPrincipal() {
		return principal;
	}

	@Override
	public Set<String> getRoles() {
		return roles;
	}

}
