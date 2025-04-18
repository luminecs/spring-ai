package org.springframework.ai.model;

public interface EmbeddingModelDescription extends ModelDescription {

	default int getDimensions() {
		return -1;
	}

}
