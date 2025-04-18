package org.springframework.ai.model.stabilityai.autoconfigure;

import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(StabilityAiConnectionProperties.CONFIG_PREFIX)
public class StabilityAiConnectionProperties extends StabilityAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.stabilityai";

	public static final String DEFAULT_BASE_URL = StabilityAiApi.DEFAULT_BASE_URL;

	public StabilityAiConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
