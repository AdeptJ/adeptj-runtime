package com.adeptj.modularweb.micro.bootstrap;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger for server startup logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class BootstrapLogger {

	private static final DateFormat FMT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS ");

	private BootstrapLogger(int logLevel) {
	}

	static void debug(String message, Throwable th) {
		log(System.out, "*DEBUG*", message, th);
	}

	static void info(String message, Throwable th) {
		log(System.out, "*INFO *", message, th);
	}

	static void warn(String message, Throwable th) {
		log(System.out, "*WARN *", message, th);
	}

	static void error(String message, Throwable th) {
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
