package org.springframework.ai.model.image.observation.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ImageObservationProperties.CONFIG_PREFIX)
public class ImageObservationProperties {

	public static final String CONFIG_PREFIX = "spring.ai.image.observations";

	private boolean includePrompt = false;

	public boolean isIncludePrompt() {
		return this.includePrompt;
	}

	public void setIncludePrompt(boolean includePrompt) {
		this.includePrompt = includePrompt;
	}

}
