package com.adeptj.modularweb.micro.bootstrap;

import java.io.IOException;
import java.util.Properties;

/**
 * Undertow configurations.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public enum Configs {

	INSTANCE;

	private Properties props;

	Configs() {
		props = new Properties();
		try {
			props.load(Configs.class.getResourceAsStream("/bootstrap.properties"));
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public String httpHost() {
		return props.getProperty("undertow.server.http.host");
	}

	public int httpPort() {
		return Integer.parseInt(props.getProperty("undertow.server.http.port"));
	}

	public boolean httpEnabled() {
		return Boolean.parseBoolean(props.getProperty("undertow.server.http.enabled"));
	}

	public String httpsHost() {
		return props.getProperty("undertow.server.https.host");
	}

	public int httpsPort() {
		return Integer.parseInt(props.getProperty("undertow.server.https.port"));
	}

	public boolean httpsEnabled() {
		return Boolean.parseBoolean(props.getProperty("undertow.server.https.enabled"));
	}

	public boolean redirectToHttp() {
		return Boolean.parseBoolean(props.getProperty("undertow.server.https.redirect-to-http"));
	}

	public int ioThreads() {
		return Integer.parseInt(props.getProperty("undertow.server.io-threads"));
	}

	public int workerThreads() {
		return Integer.parseInt(props.getProperty("undertow.server.worker-threads"));
	}

	public int bufferSize() {
		return Integer.parseInt(props.getProperty("undertow.server.buffer-size"));
	}
}
