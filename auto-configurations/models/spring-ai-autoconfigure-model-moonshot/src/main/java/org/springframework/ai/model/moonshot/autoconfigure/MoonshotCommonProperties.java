package org.springframework.ai.model.moonshot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MoonshotCommonProperties.CONFIG_PREFIX)
public class MoonshotCommonProperties extends MoonshotParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.moonshot";

	public static final String DEFAULT_BASE_URL = "https://api.moonshot.cn";

	public MoonshotCommonProperties() {
		super.setBaseUrl(DEFAULT_BASE_URL);
	}

}
