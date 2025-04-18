package org.springframework.ai.observation.conventions;

public enum AiTokenType {

// @formatter:off

	INPUT("input"),

	OUTPUT("output"),

	TOTAL("total");

	private final String value;

	AiTokenType(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
