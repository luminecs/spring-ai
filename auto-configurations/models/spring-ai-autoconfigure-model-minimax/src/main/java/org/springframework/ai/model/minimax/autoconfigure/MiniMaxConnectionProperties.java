package org.springframework.ai.model.minimax.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MiniMaxConnectionProperties.CONFIG_PREFIX)
public class MiniMaxConnectionProperties extends MiniMaxParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.minimax";

	public static final String DEFAULT_BASE_URL = "https://api.minimax.chat";

	public MiniMaxConnectionProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
