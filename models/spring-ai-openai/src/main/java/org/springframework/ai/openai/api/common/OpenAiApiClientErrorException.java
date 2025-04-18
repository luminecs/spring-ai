package org.springframework.ai.openai.api.common;

public class OpenAiApiClientErrorException extends RuntimeException {

	public OpenAiApiClientErrorException(String message) {
		super(message);
	}

	public OpenAiApiClientErrorException(String message, Throwable cause) {
		super(message, cause);
	}

}
