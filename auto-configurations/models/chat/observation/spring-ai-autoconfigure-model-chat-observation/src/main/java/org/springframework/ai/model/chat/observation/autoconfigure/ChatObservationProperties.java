package org.springframework.ai.model.chat.observation.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ChatObservationProperties.CONFIG_PREFIX)
public class ChatObservationProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.observations";

	private boolean includeCompletion = false;

	private boolean includePrompt = false;

	private boolean includeErrorLogging = false;

	public boolean isIncludeCompletion() {
		return this.includeCompletion;
	}

	public void setIncludeCompletion(boolean includeCompletion) {
		this.includeCompletion = includeCompletion;
	}

	public boolean isIncludePrompt() {
		return this.includePrompt;
	}

	public void setIncludePrompt(boolean includePrompt) {
		this.includePrompt = includePrompt;
	}

	public boolean isIncludeErrorLogging() {
		return this.includeErrorLogging;
	}

	public void setIncludeErrorLogging(boolean includeErrorLogging) {
		this.includeErrorLogging = includeErrorLogging;
	}

}
