package org.springframework.ai.model.watsonxai.autoconfigure;

import org.springframework.ai.watsonx.WatsonxAiEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(WatsonxAiEmbeddingProperties.CONFIG_PREFIX)
public class WatsonxAiEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.watsonx.ai.embedding";

	@NestedConfigurationProperty
	private WatsonxAiEmbeddingOptions options = WatsonxAiEmbeddingOptions.create()
		.withModel(WatsonxAiEmbeddingOptions.DEFAULT_MODEL);

	public String getModel() {
		return this.options.getModel();
	}

	public void setModel(String model) {
		this.options.setModel(model);
	}

	public WatsonxAiEmbeddingOptions getOptions() {
		return this.options;
	}

}
