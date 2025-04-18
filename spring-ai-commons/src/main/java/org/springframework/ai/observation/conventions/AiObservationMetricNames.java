package org.springframework.ai.observation.conventions;

public enum AiObservationMetricNames {

	OPERATION_DURATION("gen_ai.client.operation.duration"),

	TOKEN_USAGE("gen_ai.client.token.usage");

	private final String value;

	AiObservationMetricNames(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}
