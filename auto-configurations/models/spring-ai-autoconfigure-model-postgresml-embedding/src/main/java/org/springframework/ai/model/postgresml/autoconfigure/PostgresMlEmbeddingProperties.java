package org.springframework.ai.model.postgresml.autoconfigure;

import java.util.Map;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.postgresml.PostgresMlEmbeddingModel;
import org.springframework.ai.postgresml.PostgresMlEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.Assert;

@ConfigurationProperties(PostgresMlEmbeddingProperties.CONFIG_PREFIX)
public class PostgresMlEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.postgresml.embedding";

	private boolean createExtension;

	@NestedConfigurationProperty
	private PostgresMlEmbeddingOptions options = PostgresMlEmbeddingOptions.builder()
		.transformer(PostgresMlEmbeddingModel.DEFAULT_TRANSFORMER_MODEL)
		.vectorType(PostgresMlEmbeddingModel.VectorType.PG_ARRAY)
		.kwargs(Map.of())
		.metadataMode(MetadataMode.EMBED)
		.build();

	public PostgresMlEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(PostgresMlEmbeddingOptions options) {
		Assert.notNull(options, "options must not be null.");
		Assert.notNull(options.getTransformer(), "transformer must not be null.");
		Assert.notNull(options.getVectorType(), "vectorType must not be null.");
		Assert.notNull(options.getKwargs(), "kwargs must not be null.");
		Assert.notNull(options.getMetadataMode(), "metadataMode must not be null.");

		this.options = options;
	}

	public boolean isCreateExtension() {
		return this.createExtension;
	}

	public void setCreateExtension(boolean createExtension) {
		this.createExtension = createExtension;
	}

}
