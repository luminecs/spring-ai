package org.springframework.ai.model.minimax.autoconfigure;

import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MiniMaxChatProperties.CONFIG_PREFIX)
public class MiniMaxChatProperties extends MiniMaxParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.minimax.chat";

	public static final String DEFAULT_CHAT_MODEL = MiniMaxApi.ChatModel.ABAB_5_5_Chat.value;

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	@NestedConfigurationProperty
	private MiniMaxChatOptions options = MiniMaxChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public MiniMaxChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(MiniMaxChatOptions options) {
		this.options = options;
	}

}
