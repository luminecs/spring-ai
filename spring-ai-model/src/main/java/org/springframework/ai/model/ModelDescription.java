package org.springframework.ai.model;

public interface ModelDescription {

	String getName();

	default String getDescription() {
		return "";
	}

	default String getVersion() {
		return "";
	}

}
