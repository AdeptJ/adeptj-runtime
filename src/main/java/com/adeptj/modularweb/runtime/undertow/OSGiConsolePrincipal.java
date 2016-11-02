package com.adeptj.modularweb.runtime.undertow;

import java.security.Principal;

import io.undertow.security.idm.PasswordCredential;

/**
 * OSGiConsolePrincipal.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsolePrincipal implements Principal {

	private String name;

	private PasswordCredential credential;

	public OSGiConsolePrincipal(String name, PasswordCredential credential) {
		this.name = name;
		this.credential = credential;
	}

	@Override
	public String getName() {
		return name;
	}

	public PasswordCredential getCredential() {
		return credential;
	}

}
