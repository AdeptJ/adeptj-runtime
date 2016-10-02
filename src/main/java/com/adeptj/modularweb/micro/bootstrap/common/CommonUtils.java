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
package com.adeptj.modularweb.micro.bootstrap.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common Utilities
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class CommonUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	private static final int BUFFER_SIZE = 1024;

	/**
	 * Deny direct instantiation.
	 */
	private CommonUtils() {
	}

	public static String toString(InputStream input) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((length = input.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		return out.toString(FrameworkConstants.UTF8);
	}

	public static boolean isPortAvailable(int port) {
		boolean isAvailable = false;
		try (ServerSocketChannel channel = ServerSocketChannel.open()) {
			channel.socket().setReuseAddress(true);
			channel.socket().bind(new InetSocketAddress(port));
			isAvailable = true;
		} catch (IOException ex) {
			LOGGER.error("Exception while aquiring port: [{}], cause:", port, ex);
			isAvailable = !(ex instanceof BindException);
		}
		return isAvailable;
	}

	public static boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac");
	}
	
	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

}
