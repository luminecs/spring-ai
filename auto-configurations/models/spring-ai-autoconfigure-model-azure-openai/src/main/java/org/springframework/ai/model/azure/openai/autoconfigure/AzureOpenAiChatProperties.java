package org.springframework.ai.model.azure.openai.autoconfigure;

import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(AzureOpenAiChatProperties.CONFIG_PREFIX)
public class AzureOpenAiChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.azure.openai.chat";

	public static final String DEFAULT_DEPLOYMENT_NAME = "gpt-4o";

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private AzureOpenAiChatOptions options = AzureOpenAiChatOptions.builder()
		.deploymentName(DEFAULT_DEPLOYMENT_NAME)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public AzureOpenAiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(AzureOpenAiChatOptions options) {
		this.options = options;
	}

}
