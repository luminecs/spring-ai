package org.springframework.ai.model.moonshot.autoconfigure;

import org.springframework.ai.moonshot.MoonshotChatOptions;
import org.springframework.ai.moonshot.api.MoonshotApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MoonshotChatProperties.CONFIG_PREFIX)
public class MoonshotChatProperties extends MoonshotParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.moonshot.chat";

	public static final String DEFAULT_CHAT_MODEL = MoonshotApi.ChatModel.MOONSHOT_V1_8K.getValue();

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private MoonshotChatOptions options = MoonshotChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public MoonshotChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(MoonshotChatOptions options) {
		this.options = options;
	}

}
