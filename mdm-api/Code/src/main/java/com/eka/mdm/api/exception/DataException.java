package com.eka.mdm.api.exception;

public class DataException extends RuntimeException {

	/**
	 * @author Ranjan.Jha
	 * 
	 *         Usually this exception will be thrown when data format is not as per
	 *         expectation.
	 */
	private static final long serialVersionUID = 1L;

	public DataException() {
		super();
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(Throwable cause) {
		super(cause);
	}
}