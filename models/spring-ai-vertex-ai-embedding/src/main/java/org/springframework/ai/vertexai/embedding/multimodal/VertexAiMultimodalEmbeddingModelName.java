package org.springframework.ai.vertexai.embedding.multimodal;

import org.springframework.ai.model.EmbeddingModelDescription;

public enum VertexAiMultimodalEmbeddingModelName implements EmbeddingModelDescription {

	MULTIMODAL_EMBEDDING_001("multimodalembedding@001", "001", 1408, "Multimodal model");

	private final String modelVersion;

	private final String modelName;

	private final String description;

	private final int dimensions;

	VertexAiMultimodalEmbeddingModelName(String value, String modelVersion, int dimensions, String description) {
		this.modelName = value;
		this.modelVersion = modelVersion;
		this.dimensions = dimensions;
		this.description = description;
	}

	@Override
	public String getName() {
		return this.modelName;
	}

	@Override
	public String getVersion() {
		return this.modelVersion;
	}

	@Override
	public int getDimensions() {
		return this.dimensions;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

}
