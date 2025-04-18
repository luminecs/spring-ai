package org.springframework.ai.observation.conventions;

public enum AiOperationType {

	// @formatter:off

	CHAT("chat"),

	EMBEDDING("embedding"),

	FRAMEWORK("framework"),

	IMAGE("image"),

	TEXT_COMPLETION("text_completion");

	private final String value;

	AiOperationType(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	// @formatter:on

}
