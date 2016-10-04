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
package com.adeptj.modularweb.micro.common;

import static com.adeptj.modularweb.micro.common.Constants.KEY_BROWSERS;
import static com.adeptj.modularweb.micro.common.Constants.MAC_BROWSER_LAUNCH_CMD;
import static com.adeptj.modularweb.micro.common.Constants.OS;
import static com.adeptj.modularweb.micro.common.Constants.WIN_BROWSER_LAUNCH_CMD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adeptj.modularweb.micro.config.Configs;

/**
 * Common Utilities
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	private static final int BUFFER_SIZE = 1024;

	public static final String REGEX_COMMA = ",";

    public static final String EMPTY = "";

    public static final String SPACE = " ";

    public static final String PIPE = " || ";

    public static final String CMD_SH = "sh";

    public static final String CMD_OPT = "-c";

    public static final int INDEX_ZERO = 0;

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
			out.write(buffer, INDEX_ZERO, length);
		}
		return out.toString(Constants.UTF8);
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
		return OS.startsWith("Mac");
	}

	public static boolean isWindows() {
		return OS.startsWith("Windows");
	}

	public static boolean isUnix() {
		String os = OS.toLowerCase();
		return os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
	}

	public static void launchBrowser(URL url) throws IOException {
		if (isMac()) {
			Runtime.getRuntime().exec(MAC_BROWSER_LAUNCH_CMD + url);
		} else if (isWindows()) {
			Runtime.getRuntime().exec(WIN_BROWSER_LAUNCH_CMD + url);
		} else if (isUnix()) {
			String[] browsers = Configs.INSTANCE.main().getString(KEY_BROWSERS).split(REGEX_COMMA);
			StringBuilder cmdBuilder = new StringBuilder();
			int index = INDEX_ZERO;
			for (String browser : browsers) {
				if (index == INDEX_ZERO) {
					cmdBuilder.append(EMPTY).append(browser).append(SPACE).append(url);
				} else {
					cmdBuilder.append(PIPE).append(browser).append(SPACE).append(url);
				}
				index++;
			}
			Runtime.getRuntime().exec(new String[] {CMD_SH, CMD_OPT, cmdBuilder.toString() });
		}
	}
}
