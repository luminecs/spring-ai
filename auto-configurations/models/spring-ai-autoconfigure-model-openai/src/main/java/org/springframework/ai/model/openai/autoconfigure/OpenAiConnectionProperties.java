package org.springframework.ai.model.openai.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OpenAiConnectionProperties.CONFIG_PREFIX)
public class OpenAiConnectionProperties extends OpenAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.openai";

	public static final String DEFAULT_BASE_URL = "https://api.openai.com";

	public OpenAiConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
