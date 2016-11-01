package com.adeptj.modularweb.runtime.osgi;

import java.security.Principal;

/**
 * OSGiConsolePrincipal.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsolePrincipal implements Principal {

	private String name;

	public OSGiConsolePrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
