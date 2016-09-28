/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
*/
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
