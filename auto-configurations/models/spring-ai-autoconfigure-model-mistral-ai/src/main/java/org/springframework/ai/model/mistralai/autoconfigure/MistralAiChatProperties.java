package org.springframework.ai.model.mistralai.autoconfigure;

import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(MistralAiChatProperties.CONFIG_PREFIX)
public class MistralAiChatProperties extends MistralAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mistralai.chat";

	public static final String DEFAULT_CHAT_MODEL = MistralAiApi.ChatModel.SMALL.getValue();

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	private static final Double DEFAULT_TOP_P = 1.0;

	private static final Boolean IS_ENABLED = false;

	@NestedConfigurationProperty
	private MistralAiChatOptions options = MistralAiChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.safePrompt(!IS_ENABLED)
		.topP(DEFAULT_TOP_P)
		.build();

	public MistralAiChatProperties() {
		super.setBaseUrl(MistralAiCommonProperties.DEFAULT_BASE_URL);
	}

	public MistralAiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(MistralAiChatOptions options) {
		this.options = options;
	}

}
