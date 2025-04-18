package org.springframework.ai.retry;

public class NonTransientAiException extends RuntimeException {

	public NonTransientAiException(String message) {
		super(message);
	}

	public NonTransientAiException(String message, Throwable cause) {
		super(message, cause);
	}

}
