package com.adeptj.runtime.exception;

/**
 * SystemException: Exception thrown by AdeptJ Runtime.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class SystemException extends RuntimeException {

	private static final long serialVersionUID = 2206058386357128479L;

	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemException(String message) {
		super(message);
	}

	public SystemException(Throwable cause) {
		super(cause);
	}

}