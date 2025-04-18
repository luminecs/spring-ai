package org.springframework.ai.retry;

public class TransientAiException extends RuntimeException {

	public TransientAiException(String message) {
		super(message);
	}

	public TransientAiException(String message, Throwable cause) {
		super(message, cause);
	}

}
