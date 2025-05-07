package org.springframework.ai.vertexai.embedding;

import java.io.IOException;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;

import org.springframework.util.StringUtils;

public class VertexAiEmbeddingConnectionDetails {

	public static final String DEFAULT_ENDPOINT = "us-central1-aiplatform.googleapis.com:443";

	public static final String DEFAULT_ENDPOINT_SUFFIX = "-aiplatform.googleapis.com:443";

	public static final String DEFAULT_PUBLISHER = "google";

	private static final String DEFAULT_LOCATION = "us-central1";

	private final String projectId;

	private final String location;

	private final String publisher;

	private final PredictionServiceSettings predictionServiceSettings;

	public VertexAiEmbeddingConnectionDetails(String projectId, String location, String publisher,
			PredictionServiceSettings predictionServiceSettings) {
		this.projectId = projectId;
		this.location = location;
		this.publisher = publisher;
		this.predictionServiceSettings = predictionServiceSettings;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getProjectId() {
		return this.projectId;
	}

	public String getLocation() {
		return this.location;
	}

	public String getPublisher() {
		return this.publisher;
	}

	public EndpointName getEndpointName(String modelName) {
		return EndpointName.ofProjectLocationPublisherModelName(this.projectId, this.location, this.publisher,
				modelName);
	}

	public PredictionServiceSettings getPredictionServiceSettings() {
		return this.predictionServiceSettings;
	}

	public static class Builder {

		private String endpoint;

		private String projectId;

		private String location;

		private String publisher;

		private PredictionServiceSettings predictionServiceSettings;

		public Builder apiEndpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}

		public Builder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public Builder location(String location) {
			this.location = location;
			return this;
		}

		public Builder publisher(String publisher) {
			this.publisher = publisher;
			return this;
		}

		public Builder predictionServiceSettings(PredictionServiceSettings predictionServiceSettings) {
			this.predictionServiceSettings = predictionServiceSettings;
			return this;
		}

		public VertexAiEmbeddingConnectionDetails build() {
			if (!StringUtils.hasText(this.endpoint)) {
				if (!StringUtils.hasText(this.location)) {
					this.endpoint = DEFAULT_ENDPOINT;
					this.location = DEFAULT_LOCATION;
				}
				else {
					this.endpoint = this.location + DEFAULT_ENDPOINT_SUFFIX;
				}
			}

			if (!StringUtils.hasText(this.publisher)) {
				this.publisher = DEFAULT_PUBLISHER;
			}

			if (this.predictionServiceSettings == null) {
				try {
					this.predictionServiceSettings = PredictionServiceSettings.newBuilder()
						.setEndpoint(this.endpoint)
						.build();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			return new VertexAiEmbeddingConnectionDetails(this.projectId, this.location, this.publisher,
					this.predictionServiceSettings);
		}

	}

}
