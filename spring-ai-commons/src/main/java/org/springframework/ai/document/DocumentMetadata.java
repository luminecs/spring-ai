package org.springframework.ai.document;

public enum DocumentMetadata {

// @formatter:off

	DISTANCE("distance");

	private final String value;

	DocumentMetadata(String value) {
		this.value = value;
	}
	public String value() {
		return this.value;
	}

// @formatter:on

	@Override
	public String toString() {
		return this.value;
	}

}
