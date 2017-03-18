package com.adeptj.runtime.exception;

/**
 * InitializationException: Exception thrown by AdeptJ Runtime initialization code.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class InitializationException extends RuntimeException {

	private static final long serialVersionUID = 6443421220206654015L;

	public InitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitializationException(String message) {
		super(message);
	}

	public InitializationException(Throwable cause) {
		super(cause);
	}

}