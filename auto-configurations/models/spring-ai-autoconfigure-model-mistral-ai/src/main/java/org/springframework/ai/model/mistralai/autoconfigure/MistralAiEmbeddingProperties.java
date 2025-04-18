package org.springframework.ai.model.mistralai.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MistralAiEmbeddingProperties.CONFIG_PREFIX)
public class MistralAiEmbeddingProperties extends MistralAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mistralai.embedding";

	public static final String DEFAULT_EMBEDDING_MODEL = MistralAiApi.EmbeddingModel.EMBED.getValue();

	public static final String DEFAULT_ENCODING_FORMAT = "float";

	public MetadataMode metadataMode = MetadataMode.EMBED;

	@NestedConfigurationProperty
	private MistralAiEmbeddingOptions options = MistralAiEmbeddingOptions.builder()
		.withModel(DEFAULT_EMBEDDING_MODEL)
		.withEncodingFormat(DEFAULT_ENCODING_FORMAT)
		.build();

	public MistralAiEmbeddingProperties() {
		super.setBaseUrl(MistralAiCommonProperties.DEFAULT_BASE_URL);
	}

	public MistralAiEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(MistralAiEmbeddingOptions options) {
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

}
