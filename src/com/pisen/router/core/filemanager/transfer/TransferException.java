package com.pisen.router.core.filemanager.transfer;

/**
 * 传输异常
 * 
 * @author yangyp
 */
@SuppressWarnings("serial")
public class TransferException extends RuntimeException {

	private final TransferStatus mFinalStatus;

	public TransferException(TransferStatus finalStatus, String message) {
		super(message);
		mFinalStatus = finalStatus;
	}

	public TransferException(TransferStatus finalStatus, Throwable t) {
		this(finalStatus, t.getMessage());
		initCause(t);
	}

	public TransferException(TransferStatus finalStatus, String message, Throwable t) {
		this(finalStatus, message);
		initCause(t);
	}

	public TransferStatus getFinalStatus() {
		return mFinalStatus;
	}

	public static TransferException throwUnhandledHttpError(TransferStatus code, String message) throws TransferException {
		final String error = "Unhandled HTTP response: " + code + "[" + code.value + "] " + message;
		throw new TransferException(code, error);

	}
}
