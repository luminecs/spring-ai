package org.springframework.ai.embedding.observation;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.observation.ModelObservationContext;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.observation.conventions.AiOperationType;

public class EmbeddingModelObservationContext extends ModelObservationContext<EmbeddingRequest, EmbeddingResponse> {

	EmbeddingModelObservationContext(EmbeddingRequest embeddingRequest, String provider) {
		super(embeddingRequest,
				AiOperationMetadata.builder()
					.operationType(AiOperationType.EMBEDDING.value())
					.provider(provider)
					.build());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private EmbeddingRequest embeddingRequest;

		private String provider;

		private Builder() {
		}

		public Builder embeddingRequest(EmbeddingRequest embeddingRequest) {
			this.embeddingRequest = embeddingRequest;
			return this;
		}

		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public EmbeddingModelObservationContext build() {
			return new EmbeddingModelObservationContext(this.embeddingRequest, this.provider);
		}

	}

}
