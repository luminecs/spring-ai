package org.springframework.ai.observation.conventions;

public enum AiObservationEventNames {

// @formatter:off

	CONTENT_PROMPT("gen_ai.content.prompt"),

	CONTENT_COMPLETION("gen_ai.content.completion");

	private final String value;

	AiObservationEventNames(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
