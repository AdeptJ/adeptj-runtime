package com.adeptj.modularweb.runtime.osgi;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import io.undertow.security.idm.Account;

/**
 * OSGiConsoleAccount.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsoleAccount implements Account {

	private static final long serialVersionUID = -2090504892837494810L;
	
	private String loginId;
	
	char[] credential;

	public OSGiConsoleAccount(String loginId, char[] credential) {
		this.loginId = loginId;
		this.credential = credential;
	}

	@Override
	public Principal getPrincipal() {
		return new OSGiConsolePrincipal(loginId);
	}

	@Override
	public Set<String> getRoles() {
		return Collections.singleton("OSGiAdmin");
	}

}
