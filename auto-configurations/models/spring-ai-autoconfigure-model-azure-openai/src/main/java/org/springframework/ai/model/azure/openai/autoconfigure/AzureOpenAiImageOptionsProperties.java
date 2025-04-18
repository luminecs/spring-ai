package org.springframework.ai.model.azure.openai.autoconfigure;

import org.springframework.ai.azure.openai.AzureOpenAiImageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(AzureOpenAiImageOptionsProperties.CONFIG_PREFIX)
public class AzureOpenAiImageOptionsProperties {

	public static final String CONFIG_PREFIX = "spring.ai.azure.openai.image";

	@NestedConfigurationProperty
	private AzureOpenAiImageOptions options = AzureOpenAiImageOptions.builder().build();

	public AzureOpenAiImageOptions getOptions() {
		return this.options;
	}

	public void setOptions(AzureOpenAiImageOptions options) {
		this.options = options;
	}

}
