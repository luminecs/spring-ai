package org.springframework.ai.observation;

import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.util.Assert;

public record AiOperationMetadata(String operationType, String provider) {

	public AiOperationMetadata {
		Assert.hasText(operationType, "operationType cannot be null or empty");
		Assert.hasText(provider, "provider cannot be null or empty");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private String operationType;

		private String provider;

		private Builder() {
		}

		public Builder operationType(String operationType) {
			this.operationType = operationType;
			return this;
		}

		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public AiOperationMetadata build() {
			return new AiOperationMetadata(this.operationType, this.provider);
		}

	}

}
