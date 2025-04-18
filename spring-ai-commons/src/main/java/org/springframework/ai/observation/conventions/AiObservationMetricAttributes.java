package org.springframework.ai.observation.conventions;

public enum AiObservationMetricAttributes {

// @formatter:off

	TOKEN_TYPE("gen_ai.token.type");

	private final String value;

	AiObservationMetricAttributes(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
