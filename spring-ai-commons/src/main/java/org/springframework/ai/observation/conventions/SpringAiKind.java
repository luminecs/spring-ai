package org.springframework.ai.observation.conventions;

public enum SpringAiKind {

	// @formatter:off

	ADVISOR("advisor"),

	CHAT_CLIENT("chat_client"),

	VECTOR_STORE("vector_store");

	private final String value;

	SpringAiKind(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	// @formatter:on

}
