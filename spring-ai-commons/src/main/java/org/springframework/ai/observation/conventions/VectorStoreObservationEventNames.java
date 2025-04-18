package org.springframework.ai.observation.conventions;

public enum VectorStoreObservationEventNames {

// @formatter:off

	CONTENT_QUERY_RESPONSE("db.vector.content.query.response");

	private final String value;

	VectorStoreObservationEventNames(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

// @formatter:on

}
