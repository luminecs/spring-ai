package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(OpenAiModerationProperties.CONFIG_PREFIX)
public class OpenAiModerationProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai.moderation";

	@NestedConfigurationProperty
	private OpenAiModerationOptions options = OpenAiModerationOptions.builder().build();

	public OpenAiModerationOptions getOptions() {
		return this.options;
	}

	public void setOptions(OpenAiModerationOptions options) {
		this.options = options;
	}

}
