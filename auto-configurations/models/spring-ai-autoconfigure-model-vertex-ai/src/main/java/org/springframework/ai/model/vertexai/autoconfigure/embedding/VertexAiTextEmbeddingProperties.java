package org.springframework.ai.model.vertexai.autoconfigure.embedding;

import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(VertexAiTextEmbeddingProperties.CONFIG_PREFIX)
public class VertexAiTextEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vertex.ai.embedding.text";

	private VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
		.taskType(VertexAiTextEmbeddingOptions.TaskType.RETRIEVAL_DOCUMENT)
		.model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
		.build();

	public VertexAiTextEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(VertexAiTextEmbeddingOptions options) {
		this.options = options;
	}

}
