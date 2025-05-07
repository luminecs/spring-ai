package org.springframework.ai.vertexai.embedding.text;

import java.util.List;

import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class VertexAiTextEmbeddingRetryTests {

	private TestRetryListener retryListener;

	private RetryTemplate retryTemplate;

	@Mock
	private PredictionServiceClient mockPredictionServiceClient;

	@Mock
	private VertexAiEmbeddingConnectionDetails mockConnectionDetails;

	@Mock
	private PredictRequest.Builder mockPredictRequestBuilder;

	@Mock
	private PredictionServiceSettings mockPredictionServiceSettings;

	private TestVertexAiTextEmbeddingModel embeddingModel;

	@BeforeEach
	public void setUp() {
		this.retryTemplate = RetryUtils.SHORT_RETRY_TEMPLATE;
		this.retryListener = new TestRetryListener();
		this.retryTemplate.registerListener(this.retryListener);

		this.embeddingModel = new TestVertexAiTextEmbeddingModel(this.mockConnectionDetails,
				VertexAiTextEmbeddingOptions.builder().build(), this.retryTemplate);
		this.embeddingModel.setMockPredictionServiceClient(this.mockPredictionServiceClient);
		this.embeddingModel.setMockPredictRequestBuilder(this.mockPredictRequestBuilder);
		given(this.mockPredictRequestBuilder.build()).willReturn(PredictRequest.getDefaultInstance());
	}

	@Test
	public void vertexAiEmbeddingTransientError() {

		PredictResponse mockResponse = PredictResponse.newBuilder()
			.addPredictions(Value.newBuilder()
				.setStructValue(Struct.newBuilder()
					.putFields("embeddings", Value.newBuilder()
						.setStructValue(Struct.newBuilder()
							.putFields("values",
									Value.newBuilder()
										.setListValue(com.google.protobuf.ListValue.newBuilder()
											.addValues(Value.newBuilder().setNumberValue(9.9))
											.addValues(Value.newBuilder().setNumberValue(8.8))
											.build())
										.build())
							.putFields("statistics",
									Value.newBuilder()
										.setStructValue(Struct.newBuilder()
											.putFields("token_count", Value.newBuilder().setNumberValue(10).build())
											.build())
										.build())
							.build())
						.build())
					.build())
				.build())
			.build();

		given(this.mockPredictionServiceClient.predict(any())).willThrow(new TransientAiException("Transient Error 1"))
			.willThrow(new TransientAiException("Transient Error 2"))
			.willReturn(mockResponse);

		EmbeddingOptions options = VertexAiTextEmbeddingOptions.builder().model("model").build();
		EmbeddingResponse result = this.embeddingModel.call(new EmbeddingRequest(List.of("text1", "text2"), options));

		assertThat(result).isNotNull();
		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).getOutput()).isEqualTo(new float[] { 9.9f, 8.8f });
		assertThat(this.retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(this.retryListener.onErrorRetryCount).isEqualTo(2);

		verify(this.mockPredictRequestBuilder, times(3)).build();
	}

	@Test
	public void vertexAiEmbeddingNonTransientError() {

		given(this.mockPredictionServiceClient.predict(any())).willThrow(new RuntimeException("Non Transient Error"));

		EmbeddingOptions options = VertexAiTextEmbeddingOptions.builder().model("model").build();

		assertThatThrownBy(() -> this.embeddingModel.call(new EmbeddingRequest(List.of("text1", "text2"), options)))
			.isInstanceOf(RuntimeException.class);

		verify(this.mockPredictionServiceClient, times(1)).predict(any());
	}

	private static class TestRetryListener implements RetryListener {

		int onErrorRetryCount = 0;

		int onSuccessRetryCount = 0;

		@Override
		public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
			this.onSuccessRetryCount = context.getRetryCount();
		}

		@Override
		public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			this.onErrorRetryCount = context.getRetryCount();
		}

	}

}
