package org.springframework.ai.model.ollama.autoconfigure;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OllamaInitializationProperties.CONFIG_PREFIX)
public class OllamaInitializationProperties {

	public static final String CONFIG_PREFIX = "spring.ai.ollama.init";

	private final ModelTypeInit chat = new ModelTypeInit();

	private final ModelTypeInit embedding = new ModelTypeInit();

	private PullModelStrategy pullModelStrategy = PullModelStrategy.NEVER;

	private Duration timeout = Duration.ofMinutes(5);

	private int maxRetries = 0;

	public PullModelStrategy getPullModelStrategy() {
		return this.pullModelStrategy;
	}

	public void setPullModelStrategy(PullModelStrategy pullModelStrategy) {
		this.pullModelStrategy = pullModelStrategy;
	}

	public ModelTypeInit getChat() {
		return this.chat;
	}

	public ModelTypeInit getEmbedding() {
		return this.embedding;
	}

	public Duration getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public int getMaxRetries() {
		return this.maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public static class ModelTypeInit {

		private boolean include = true;

		private List<String> additionalModels = List.of();

		public boolean isInclude() {
			return this.include;
		}

		public void setInclude(boolean include) {
			this.include = include;
		}

		public List<String> getAdditionalModels() {
			return this.additionalModels;
		}

		public void setAdditionalModels(List<String> additionalModels) {
			this.additionalModels = additionalModels;
		}

	}

}
