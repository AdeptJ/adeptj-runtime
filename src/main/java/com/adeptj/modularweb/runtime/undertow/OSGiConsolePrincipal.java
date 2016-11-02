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
	
	public PasswordCredential getCredential() {
		return credential;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OSGiConsolePrincipal other = (OSGiConsolePrincipal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OSGiConsolePrincipal [name=" + name + "]";
	}
}
