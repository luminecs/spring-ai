package org.springframework.ai.model.qianfan.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.qianfan.QianFanEmbeddingOptions;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(QianFanEmbeddingProperties.CONFIG_PREFIX)
public class QianFanEmbeddingProperties extends QianFanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.qianfan.embedding";

	private MetadataMode metadataMode = MetadataMode.EMBED;

	@NestedConfigurationProperty
	private QianFanEmbeddingOptions options = QianFanEmbeddingOptions.builder()
		.model(QianFanApi.DEFAULT_EMBEDDING_MODEL)
		.build();

	public QianFanEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(QianFanEmbeddingOptions options) {
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

}
