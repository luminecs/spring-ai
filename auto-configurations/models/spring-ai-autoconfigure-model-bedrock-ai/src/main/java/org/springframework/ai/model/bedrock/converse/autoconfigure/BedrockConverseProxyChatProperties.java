package org.springframework.ai.model.bedrock.converse.autoconfigure;

import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.Assert;

@ConfigurationProperties(BedrockConverseProxyChatProperties.CONFIG_PREFIX)
public class BedrockConverseProxyChatProperties {

	public static final String CONFIG_PREFIX = "spring.ai.bedrock.converse.chat";

	@NestedConfigurationProperty
	private ToolCallingChatOptions options = ToolCallingChatOptions.builder()
		.temperature(0.7)
		.maxTokens(300)
		.topK(10)
		.build();

	public ToolCallingChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(ToolCallingChatOptions options) {
		Assert.notNull(options, "FunctionCallingOptions must not be null");
		this.options = options;
	}

}
