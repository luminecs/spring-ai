package org.springframework.ai.observation.conventions;

public enum VectorStoreSimilarityMetric {

	// @formatter:off

	COSINE("cosine"),

	DOT("dot"),

	EUCLIDEAN("euclidean"),

	MANHATTAN("manhattan");

	private final String value;

	VectorStoreSimilarityMetric(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	// @formatter:on

}
