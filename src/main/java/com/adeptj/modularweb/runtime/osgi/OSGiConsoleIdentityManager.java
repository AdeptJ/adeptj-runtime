package com.adeptj.modularweb.runtime.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.common.OSGiConsolePasswords;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OSGiConsoleIdentityManager.class);

	/**
	 * This is queried on each request afterward.
	 */
	@Override
	public Account verify(Account account) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(Account account)");
		return new OSGiConsoleAccount(account.getPrincipal().getName(), null);
	}

	/**
	 * Called by FormAuthenticationMechanism.
	 */
	@Override
	public Account verify(String id, Credential credential) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(String id, Credential credential)");
		PasswordCredential passwordCredential = (PasswordCredential) credential;
		char[] password = passwordCredential.getPassword();
		if (OSGiConsolePasswords.INSTANCE.matches(new String(password))) {
			return new OSGiConsoleAccount(id, password);
		}
		return null;
	}

	@Override
	public Account verify(Credential credential) {
		LOGGER.info("OSGiConsoleIdentityManager.verify(Credential credential)");
		return null;
	}

}
