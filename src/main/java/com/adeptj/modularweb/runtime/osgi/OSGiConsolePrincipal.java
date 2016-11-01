package com.adeptj.modularweb.runtime.osgi;

import java.security.Principal;

/**
 * OSGiConsolePrincipal.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsolePrincipal implements Principal {

	@Override
	public String getName() {
		return "OSGiConsolePrincipal";
	}

}
