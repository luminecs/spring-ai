package org.springframework.ai.model.minimax.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MiniMaxEmbeddingProperties.CONFIG_PREFIX)
public class MiniMaxEmbeddingProperties extends MiniMaxParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.minimax.embedding";

	public static final String DEFAULT_EMBEDDING_MODEL = MiniMaxApi.EmbeddingModel.Embo_01.value;

	private MetadataMode metadataMode = MetadataMode.EMBED;

	@NestedConfigurationProperty
	private MiniMaxEmbeddingOptions options = MiniMaxEmbeddingOptions.builder().model(DEFAULT_EMBEDDING_MODEL).build();

	public MiniMaxEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(MiniMaxEmbeddingOptions options) {
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

}
