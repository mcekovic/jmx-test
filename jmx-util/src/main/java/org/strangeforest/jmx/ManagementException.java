package org.strangeforest.jmx;

public class ManagementException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ManagementException(String message) {
		super(message);
	}

	public ManagementException(Exception exception) {
		super(exception);
	}
}
