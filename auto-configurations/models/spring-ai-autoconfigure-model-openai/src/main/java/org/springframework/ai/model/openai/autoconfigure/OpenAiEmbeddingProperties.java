package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiEmbeddingProperties.CONFIG_PREFIX)
public class OpenAiEmbeddingProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.embedding";

	public static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-ada-002";

	public static final String DEFAULT_EMBEDDINGS_PATH = "/v1/embeddings";

	private MetadataMode metadataMode = MetadataMode.EMBED;

	private String embeddingsPath = DEFAULT_EMBEDDINGS_PATH;

	@NestedConfigurationProperty
	private OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder().model(DEFAULT_EMBEDDING_MODEL).build();

	public OpenAiEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiEmbeddingOptions options) {
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

	public String getEmbeddingsPath() {
		return this.embeddingsPath;
	}

	public void setEmbeddingsPath(String embeddingsPath) {
		this.embeddingsPath = embeddingsPath;
	}

}
