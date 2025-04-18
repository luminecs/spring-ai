package org.springframework.ai.embedding.observation;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.observation.ModelObservationContext;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.util.Assert;

public class EmbeddingModelObservationContext extends ModelObservationContext<EmbeddingRequest, EmbeddingResponse> {

	private final EmbeddingOptions requestOptions;

	EmbeddingModelObservationContext(EmbeddingRequest embeddingRequest, String provider,
			EmbeddingOptions requestOptions) {
		super(embeddingRequest,
				AiOperationMetadata.builder()
					.operationType(AiOperationType.EMBEDDING.value())
					.provider(provider)
					.build());
		Assert.notNull(requestOptions, "requestOptions cannot be null");
		this.requestOptions = requestOptions;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Deprecated(forRemoval = true)
	public EmbeddingOptions getRequestOptions() {
		return this.requestOptions;
	}

	public static final class Builder {

		private EmbeddingRequest embeddingRequest;

		private String provider;

		private EmbeddingOptions requestOptions;

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

		@Deprecated(forRemoval = true)
		public Builder requestOptions(EmbeddingOptions requestOptions) {
			this.requestOptions = requestOptions;
			return this;
		}

		public EmbeddingModelObservationContext build() {
			return new EmbeddingModelObservationContext(this.embeddingRequest, this.provider, this.requestOptions);
		}

	}

}
