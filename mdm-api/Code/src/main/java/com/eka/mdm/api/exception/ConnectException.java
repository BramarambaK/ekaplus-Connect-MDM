package com.eka.mdm.api.exception;

public class ConnectException extends RuntimeException {

	/**
	 * @author Ranjan.Jha
	 * 
	 *         Usually this exception will be thrown at time of connection error
	 *         while trying to fetch/call connect data.
	 */
	private static final long serialVersionUID = 1L;

	public ConnectException() {
		super();
	}

	public ConnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectException(String message) {
		super(message);
	}

	public ConnectException(Throwable cause) {
		super(cause);
	}

}
