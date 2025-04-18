package org.springframework.ai.image.observation;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.observation.ModelObservationContext;
import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.util.Assert;

public class ImageModelObservationContext extends ModelObservationContext<ImagePrompt, ImageResponse> {

	private final ImageOptions requestOptions;

	ImageModelObservationContext(ImagePrompt imagePrompt, String provider, ImageOptions requestOptions) {
		super(imagePrompt,
				AiOperationMetadata.builder().operationType(AiOperationType.IMAGE.value()).provider(provider).build());
		Assert.notNull(requestOptions, "requestOptions cannot be null");
		this.requestOptions = requestOptions;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Deprecated(forRemoval = true)
	public ImageOptions getRequestOptions() {
		return this.requestOptions;
	}

	public String getOperationType() {
		return AiOperationType.IMAGE.value();
	}

	public static final class Builder {

		private ImagePrompt imagePrompt;

		private String provider;

		private ImageOptions requestOptions;

		private Builder() {
		}

		public Builder imagePrompt(ImagePrompt imagePrompt) {
			this.imagePrompt = imagePrompt;
			return this;
		}

		public Builder provider(String provider) {
			this.provider = provider;
			return this;
		}

		@Deprecated(forRemoval = true)
		public Builder requestOptions(ImageOptions requestOptions) {
			this.requestOptions = requestOptions;
			return this;
		}

		public ImageModelObservationContext build() {
			return new ImageModelObservationContext(this.imagePrompt, this.provider, this.requestOptions);
		}

	}

}
