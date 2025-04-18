package org.springframework.ai.model.zhipuai.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(ZhiPuAiEmbeddingProperties.CONFIG_PREFIX)
public class ZhiPuAiEmbeddingProperties extends ZhiPuAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.zhipuai.embedding";

	public static final String DEFAULT_EMBEDDING_MODEL = ZhiPuAiApi.EmbeddingModel.Embedding_2.value;

	private MetadataMode metadataMode = MetadataMode.EMBED;

	@NestedConfigurationProperty
	private ZhiPuAiEmbeddingOptions options = ZhiPuAiEmbeddingOptions.builder().model(DEFAULT_EMBEDDING_MODEL).build();

	public ZhiPuAiEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(ZhiPuAiEmbeddingOptions options) {
		this.options = options;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

}
