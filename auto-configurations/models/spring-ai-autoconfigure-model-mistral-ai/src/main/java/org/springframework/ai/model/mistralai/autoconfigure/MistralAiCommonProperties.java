package org.springframework.ai.model.mistralai.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MistralAiCommonProperties.CONFIG_PREFIX)
public class MistralAiCommonProperties extends MistralAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mistralai";

	public static final String DEFAULT_BASE_URL = "https://api.mistral.ai";

	public MistralAiCommonProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
