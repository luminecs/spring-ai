package org.springframework.ai.model.chat.client.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(ChatClientBuilderProperties.CONFIG_PREFIX)
public class ChatClientBuilderProperties {

	public static final String CONFIG_PREFIX = "spring.ai.chat.client";

	private boolean enabled = true;

	private Observations observations = new Observations();

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

		private boolean includeInput = false;

		public boolean isIncludeInput() {
			return this.includeInput;
		}

		public void setIncludeInput(boolean includeCompletion) {
			this.includeInput = includeCompletion;
		}

	}

}
