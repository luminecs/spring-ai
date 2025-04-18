package org.springframework.ai.model.anthropic.autoconfigure;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(AnthropicChatProperties.CONFIG_PREFIX)
public class AnthropicChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.anthropic.chat";

	@NestedConfigurationProperty
	private AnthropicChatOptions options = AnthropicChatOptions.builder()
		.model(AnthropicChatModel.DEFAULT_MODEL_NAME)
		.maxTokens(AnthropicChatModel.DEFAULT_MAX_TOKENS)
		.temperature(AnthropicChatModel.DEFAULT_TEMPERATURE)
		.build();

	public AnthropicChatOptions getOptions() {
		return this.options;
	}

}
