package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiChatProperties.CONFIG_PREFIX)
public class OpenAiChatProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.chat";

	public static final String DEFAULT_CHAT_MODEL = "gpt-4o-mini";

	public static final String DEFAULT_COMPLETIONS_PATH = "/v1/chat/completions";

	private static final Double DEFAULT_TEMPERATURE = 0.7;

	private String completionsPath = DEFAULT_COMPLETIONS_PATH;

	@NestedConfigurationProperty
	private OpenAiChatOptions options = OpenAiChatOptions.builder()
		.model(DEFAULT_CHAT_MODEL)
		.temperature(DEFAULT_TEMPERATURE)
		.build();

	public OpenAiChatOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiChatOptions options) {
		this.options = options;
	}

	public String getCompletionsPath() {
		return this.completionsPath;
	}

	public void setCompletionsPath(String completionsPath) {
		this.completionsPath = completionsPath;
	}

}
