package org.springframework.ai.model.chat.client.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ChatClientBuilderProperties.CONFIG_PREFIX)
public class ChatClientBuilderProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.client";

	private boolean enabled = true;

	private final Observations observations = new Observations();

	public Observations getObservations() {
		return this.observations;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static class Observations {

		private boolean includePrompt = false;

		public boolean isIncludePrompt() {
			return this.includePrompt;
		}

		public void setIncludePrompt(boolean includePrompt) {
			this.includePrompt = includePrompt;
		}

	}

}
