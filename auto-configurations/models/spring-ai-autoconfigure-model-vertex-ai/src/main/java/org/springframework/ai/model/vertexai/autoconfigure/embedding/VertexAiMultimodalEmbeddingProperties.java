package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import org.springframework.ai.vertexai.embedding.multimodal.VertexAiMultimodalEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(VertexAiMultimodalEmbeddingProperties.CONFIG_PREFIX)
public class VertexAiMultimodalEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vertex.ai.embedding.multimodal";

	private VertexAiMultimodalEmbeddingOptions options = VertexAiMultimodalEmbeddingOptions.builder()
		.model(VertexAiMultimodalEmbeddingOptions.DEFAULT_MODEL_NAME)
		.build();

	public VertexAiMultimodalEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(VertexAiMultimodalEmbeddingOptions options) {
		this.options = options;
	}

}
