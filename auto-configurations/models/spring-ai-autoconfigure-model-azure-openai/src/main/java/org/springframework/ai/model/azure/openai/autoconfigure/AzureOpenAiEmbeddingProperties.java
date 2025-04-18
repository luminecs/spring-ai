package org.springframework.ai.model.azure.openai.autoconfigure;

import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.Assert;

@ConfigurationProperties(AzureOpenAiEmbeddingProperties.CONFIG_PREFIX)
public class AzureOpenAiEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.azure.openai.embedding";

	@NestedConfigurationProperty
	private AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
		.deploymentName("text-embedding-ada-002")
		.build();

	private MetadataMode metadataMode = MetadataMode.EMBED;

	public AzureOpenAiEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(AzureOpenAiEmbeddingOptions options) {
		Assert.notNull(options, "Options must not be null");
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		Assert.notNull(metadataMode, "Metadata mode must not be null");
		this.metadataMode = metadataMode;
	}

}
