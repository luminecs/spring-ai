package org.springframework.ai.vertexai.embedding.text;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.retry.support.RetryTemplate;

public class TestVertexAiTextEmbeddingModel extends VertexAiTextEmbeddingModel {

	private PredictionServiceClient mockPredictionServiceClient;

	private PredictRequest.Builder mockPredictRequestBuilder;

	public TestVertexAiTextEmbeddingModel(VertexAiEmbeddingConnectionDetails connectionDetails,
			VertexAiTextEmbeddingOptions defaultEmbeddingOptions, RetryTemplate retryTemplate) {
		super(connectionDetails, defaultEmbeddingOptions, retryTemplate);
	}

	public void setMockPredictionServiceClient(PredictionServiceClient mockPredictionServiceClient) {
		this.mockPredictionServiceClient = mockPredictionServiceClient;
	}

	@Override
	PredictionServiceClient createPredictionServiceClient() {
		if (this.mockPredictionServiceClient != null) {
			return this.mockPredictionServiceClient;
		}
		return super.createPredictionServiceClient();
	}

	@Override
	PredictResponse getPredictResponse(PredictionServiceClient client, PredictRequest.Builder predictRequestBuilder) {
		if (this.mockPredictionServiceClient != null) {
			return this.mockPredictionServiceClient.predict(predictRequestBuilder.build());
		}
		return super.getPredictResponse(client, predictRequestBuilder);
	}

	public void setMockPredictRequestBuilder(PredictRequest.Builder mockPredictRequestBuilder) {
		this.mockPredictRequestBuilder = mockPredictRequestBuilder;
	}

	@Override
	protected PredictRequest.Builder getPredictRequestBuilder(EmbeddingRequest request, EndpointName endpointName,
			VertexAiTextEmbeddingOptions finalOptions) {
		if (this.mockPredictRequestBuilder != null) {
			return this.mockPredictRequestBuilder;
		}
		return super.getPredictRequestBuilder(request, endpointName, finalOptions);
	}

}
