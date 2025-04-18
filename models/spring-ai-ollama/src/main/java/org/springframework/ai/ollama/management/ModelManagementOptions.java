package org.springframework.ai.ollama.management;

import java.time.Duration;
import java.util.List;

public record ModelManagementOptions(PullModelStrategy pullModelStrategy, List<String> additionalModels,
		Duration timeout, Integer maxRetries) {

	public static ModelManagementOptions defaults() {
		return new ModelManagementOptions(PullModelStrategy.NEVER, List.of(), Duration.ofMinutes(5), 0);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private PullModelStrategy pullModelStrategy = PullModelStrategy.NEVER;

		private List<String> additionalModels = List.of();

		private Duration timeout = Duration.ofMinutes(5);

		private Integer maxRetries = 0;

		public Builder pullModelStrategy(PullModelStrategy pullModelStrategy) {
			this.pullModelStrategy = pullModelStrategy;
			return this;
		}

		public Builder additionalModels(List<String> additionalModels) {
			this.additionalModels = additionalModels;
			return this;
		}

		public Builder timeout(Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder maxRetries(Integer maxRetries) {
			this.maxRetries = maxRetries;
			return this;
		}

		public ModelManagementOptions build() {
			return new ModelManagementOptions(this.pullModelStrategy, this.additionalModels, this.timeout,
					this.maxRetries);
		}

	}

}
