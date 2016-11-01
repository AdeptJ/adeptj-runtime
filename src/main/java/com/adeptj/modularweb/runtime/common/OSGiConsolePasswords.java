package com.adeptj.modularweb.runtime.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.config.Configs;

/**
 * OSGiConsolePasswords, Logic copied from org.apache.felix.webconsole.internal.servlet.Password.
 * 
 * Because, we want to match the same hashing mechanism used by OSGi Web Console configuration management,
 * but classes there are package private and therefore can't be accessible to outside world.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum OSGiConsolePasswords {

	INSTANCE;

	private static final String DEFAULT_HASH_ALGO = "SHA-256";

	private final String cfgFile;

	OSGiConsolePasswords() {
		this.cfgFile = new StringBuilder(Configs.INSTANCE.felix().getString("felix-cm-dir")).append(File.separator)
				.append("org").append(File.separator).append("apache").append(File.separator).append("felix")
				.append(File.separator).append("webconsole").append(File.separator).append("internal")
				.append(File.separator).append("servlet").append(File.separator).append("OsgiManager.config")
				.toString();
	}

	public boolean matches(String id, String formPwd) {
		// This happens when OsgiManager.config file is non-existent as configuration was never saved
		// from OSGi console.
		if (Files.exists(Paths.get(this.cfgFile))) {
			return this.matchFromOsgiManagerConfigFile(formPwd);
		} else {
			// When system starts up very first time, the OsgiManager.config file is non-existent.
			// Meanwhile make use of default password maintained in provisioning file.
			return this.matchFromProvisioningConfig(id, formPwd);
		}
	}

	private boolean matchFromProvisioningConfig(String id, String formPwd) {
		Map<String, Object> users = Configs.INSTANCE.undertow().getObject("common.osgi-console-users").unwrapped();
		if (users.containsKey(id)) {
			return Arrays.equals(this.getPasswordBytes(this.hashPassword(formPwd)),
					this.getPasswordBytes((String) users.get(id)));
		}
		return false;
	}

	private boolean matchFromOsgiManagerConfigFile(String formPwd) {
		try {
			String storedPwdLine = Files.readAllLines(Paths.get(this.cfgFile)).stream().filter(line -> {
				return line.startsWith("password=");
			}).collect(Collectors.joining()).replace("\\", "");
			return Arrays.equals(this.getPasswordBytes(this.hashPassword(formPwd)), this.getPasswordBytes(
					storedPwdLine.substring(storedPwdLine.indexOf('"') + 1, storedPwdLine.length() - 1)));
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).error("IOException!!", ex);
		}
		return false;
	}

	public String hashPassword(final String hashAlgorithm, final byte[] password) {
		final String actualHashAlgo = (hashAlgorithm == null) ? DEFAULT_HASH_ALGO : hashAlgorithm;
		final byte[] hashedPassword = hashPassword(password, actualHashAlgo);
		final StringBuilder pwdBuilder = new StringBuilder(2 + actualHashAlgo.length() + hashedPassword.length * 3);
		pwdBuilder.append('{').append(actualHashAlgo.toLowerCase()).append('}');
		pwdBuilder.append(newStringUtf8(Base64.getEncoder().encode((hashedPassword))));
		return pwdBuilder.toString();
	}

	private String newStringUtf8(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private byte[] getBytesUtf8(String string) {
		if (string == null) {
			return null;
		}
		try {
			return string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Returns the given plain {@code textPassword} as an encoded hashed
	 * password string as described in the class comment.
	 *
	 * @param textPassword
	 * @return
	 * @throws NullPointerException
	 *             if {@code textPassword} is {@code null}.
	 */
	public String hashPassword(final String textPassword) {
		return hashPassword(DEFAULT_HASH_ALGO, getBytesUtf8(textPassword));
	}

	public byte[] getPasswordBytes(final String textPassword) {
		final int endHash = getEndOfHashAlgorithm(textPassword);
		if (endHash >= 0) {
			return Base64.getDecoder().decode(textPassword.substring(endHash + 1));
		}
		return getBytesUtf8(textPassword);
	}

	private int getEndOfHashAlgorithm(final String textPassword) {
		if (textPassword.startsWith("{")) {
			final int endHash = textPassword.indexOf("}");
			if (endHash > 0) {
				return endHash;
			}
		}
		return -1;
	}

	public byte[] hashPassword(final byte[] pwd, final String hashAlg) {
		// no hashing if no hash algorithm
		if (hashAlg == null || hashAlg.length() == 0) {
			return pwd;
		}
		try {
			return MessageDigest.getInstance(hashAlg).digest(pwd);
		} catch (NoSuchAlgorithmException ex) {
			LoggerFactory.getLogger(getClass()).error("NoSuchAlgorithmException!!", ex);
			throw new IllegalStateException("Cannot hash the password: " + ex);
		}
	}
}
