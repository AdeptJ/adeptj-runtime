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

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger for server startup logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class StartupLogger {

	private static final DateFormat FMT = new SimpleDateFormat(Constants.DATE_FORMAT_LOGGING);

	private StartupLogger() {
	}

	public static void debug(String message, Throwable th) {
		log(System.out, "*DEBUG*", message, th);
	}

	public static void info(String message, Throwable th) {
		log(System.out, "*INFO*", message, th);
	}

	public static void warn(String message, Throwable th) {
		log(System.out, "*WARN*", message, th);
	}

	public static void error(String message, Throwable th) {
		log(System.err, "*ERROR*", message, th);
	}

	private static void log(PrintStream out, String prefix, String message, Throwable th) {
		StringBuilder prefixBuilder = new StringBuilder();
		synchronized (FMT) {
			prefixBuilder.append(FMT.format(new Date()));
		}
		prefixBuilder.append(prefix);
		prefixBuilder.append(" [");
		prefixBuilder.append(Thread.currentThread().getName());
		prefixBuilder.append("] ");
		final String linePrefix = prefixBuilder.toString();
		synchronized (out) {
			out.print(linePrefix);
			out.println(message);
			if (th != null) {
				th.printStackTrace(new PrintStream(out) {
					public void println(String x) {
						synchronized (this) {
							print(linePrefix);
							super.println(x);
							flush();
						}
					}
				});
			}
		}
	}
}
