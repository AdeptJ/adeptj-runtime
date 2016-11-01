package com.adeptj.modularweb.runtime.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;

/**
 * OSGiConsoleIdentityManager.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class OSGiConsoleIdentityManager implements IdentityManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OSGiConsoleIdentityManager.class);

	@Override
	public Account verify(Account account) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(Account account)");
		return new OSGiConsoleAccount();
	}

	@Override
	public Account verify(String id, Credential credential) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(String id, Credential credential)");
		return new OSGiConsoleAccount();
	}

	@Override
	public Account verify(Credential credential) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(Credential credential)");
		return null;
	}

}
