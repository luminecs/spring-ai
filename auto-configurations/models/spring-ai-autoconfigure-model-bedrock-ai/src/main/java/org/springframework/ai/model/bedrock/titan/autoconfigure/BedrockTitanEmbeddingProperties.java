package org.springframework.ai.model.bedrock.titan.autoconfigure;

import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel.InputType;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(BedrockTitanEmbeddingProperties.CONFIG_PREFIX)
public class BedrockTitanEmbeddingProperties {

	public static final String CONFIG_PREFIX = "spring.ai.bedrock.titan.embedding";

	private String model = TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id();

	private InputType inputType = InputType.IMAGE;

	public static String getConfigPrefix() {
		return CONFIG_PREFIX;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public InputType getInputType() {
		return this.inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

}
