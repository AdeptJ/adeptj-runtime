package com.adeptj.modularweb.micro.common;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Utility for providing time in multiple ranges.
 * 
 * Rakesh.Kumar, AdeptJ
 */
public final class TimeUnits {
	
	// No instances, just utility methods.
	private TimeUnits() {}

	/**
	 * Converts the nanoseconds time to milliseconds.
	 * 
	 * @param startTime
	 * @return time in milliseconds
	 */
	public static final long nanosToMillis(final long startTime) {
		return NANOSECONDS.toMillis(System.nanoTime() - startTime);
	}
}
