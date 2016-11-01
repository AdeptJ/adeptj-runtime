package com.adeptj.modularweb.runtime.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.runtime.config.Configs;

/**
 * OSGiConsolePasswords, Logic from Felix
 * org.apache.felix.webconsole.internal.servlet.Password.
 * 
 * Because, we want to match the same hashing mechanism and classes there are
 * package private.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum OSGiConsolePasswords {

	INSTANCE;

	private static final String DEFAULT_HASH_ALGO = "SHA-256";

	public boolean matches(String formPwd) {
		StringBuilder pathBuilder = new StringBuilder(Configs.INSTANCE.felix().getString("felix-cm-dir"));
		pathBuilder.append(File.separator).append("org").append(File.separator).append("apache").append(File.separator)
				.append("felix").append(File.separator).append("webconsole").append(File.separator).append("internal")
				.append(File.separator).append("servlet").append(File.separator).append("OsgiManager.config");
		try {
			String storedPwdLine = Files.readAllLines(Paths.get(pathBuilder.toString())).stream()
					.filter((String line) -> {
						return line.startsWith("password=");
					}).collect(Collectors.joining()).replace("\\", "");
			String hashedPwd = storedPwdLine.substring(storedPwdLine.indexOf('"') + 1, storedPwdLine.length() - 1);
			return Arrays.equals(this.getPasswordBytes(this.hashPassword(formPwd)), this.getPasswordBytes(hashedPwd));
		} catch (IOException ex) {
			LoggerFactory.getLogger(getClass()).error("IOException!!", ex);
		}
		return false;
	}

	public String hashPassword(final String hashAlgorithm, final byte[] password) {
		final String actualHashAlgo = (hashAlgorithm == null) ? DEFAULT_HASH_ALGO : hashAlgorithm;
		final byte[] hashedPassword = hashPassword(password, actualHashAlgo);
		final StringBuffer buf = new StringBuffer(2 + actualHashAlgo.length() + hashedPassword.length * 3);
		buf.append('{').append(actualHashAlgo.toLowerCase()).append('}');
		buf.append(newStringUtf8(Base64.getEncoder().encode((hashedPassword))));
		return buf.toString();
	}

	private String newStringUtf8(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8");
		}
	}

	private byte[] getBytesUtf8(String string) {
		if (string == null) {
			return null;
		}
		try {
			return string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8");
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
		final byte[] bytePassword = getBytesUtf8(textPassword);
		return hashPassword(DEFAULT_HASH_ALGO, bytePassword);
	}

	public byte[] getPasswordBytes(final String textPassword) {
		final int endHash = getEndOfHashAlgorithm(textPassword);
		if (endHash >= 0) {
			final String encodedPassword = textPassword.substring(endHash + 1);
			return Base64.getDecoder().decode(encodedPassword);
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
			final MessageDigest md = MessageDigest.getInstance(hashAlg);
			return md.digest(pwd);
		} catch (NoSuchAlgorithmException ex) {
			LoggerFactory.getLogger(getClass()).error("NoSuchAlgorithmException!!", ex);
			throw new IllegalStateException("Cannot hash the password: " + ex);
		}
	}
}
