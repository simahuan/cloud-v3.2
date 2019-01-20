package com.pisen.router.core.filemanager;

@SuppressWarnings("serial")
public class ResourceException extends Exception {

	private final int mStatus;

	public ResourceException(int status, String message) {
		super(message);
		mStatus = status;
	}

	public ResourceException(int status, Throwable t) {
		this(status, t.getMessage());
		initCause(t);
	}

	public ResourceException(int status, String message, Throwable t) {
		this(status, message);
		initCause(t);
	}

	public int getStatus() {
		return mStatus;
	}

	public static void throwException(int status) throws ResourceException {
		throwException(status, null);
	}

	public static void throwException(int status, String message) throws ResourceException {
		final String error = "ResourceException: " + status + " " + message;
		throw new ResourceException(status, error);
	}
}
