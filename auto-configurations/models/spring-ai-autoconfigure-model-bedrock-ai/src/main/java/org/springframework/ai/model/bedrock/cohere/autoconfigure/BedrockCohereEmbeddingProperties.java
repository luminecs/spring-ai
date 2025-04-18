package org.springframework.ai.model.bedrock.cohere.autoconfigure;

import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingOptions;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest.InputType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(BedrockCohereEmbeddingProperties.CONFIG_PREFIX)
public class BedrockCohereEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.bedrock.cohere.embedding";

	private String model = CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3.id();

	@NestedConfigurationProperty
	private BedrockCohereEmbeddingOptions options = BedrockCohereEmbeddingOptions.builder()
		.inputType(InputType.SEARCH_DOCUMENT)
		.truncate(CohereEmbeddingRequest.Truncate.NONE)
		.build();

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public BedrockCohereEmbeddingOptions getOptions() {
		return this.options;
	}

	public void setOptions(BedrockCohereEmbeddingOptions options) {
		this.options = options;
	}

}
