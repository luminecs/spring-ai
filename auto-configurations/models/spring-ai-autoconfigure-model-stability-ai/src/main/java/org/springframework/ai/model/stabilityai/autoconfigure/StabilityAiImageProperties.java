package org.springframework.ai.model.stabilityai.autoconfigure;

import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(StabilityAiImageProperties.CONFIG_PREFIX)
public class StabilityAiImageProperties extends StabilityAiParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.stabilityai.image";

	@NestedConfigurationProperty
	private StabilityAiImageOptions options = StabilityAiImageOptions.builder().build();

	public StabilityAiImageOptions getOptions() {
		return this.options;
	}

	public void setOptions(StabilityAiImageOptions options) {
		this.options = options;
	}

}
