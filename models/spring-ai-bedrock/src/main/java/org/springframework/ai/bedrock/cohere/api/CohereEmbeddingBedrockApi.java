package org.springframework.ai.bedrock.cohere.api;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.api.AbstractBedrockApi;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingResponse;

public class CohereEmbeddingBedrockApi
		extends AbstractBedrockApi<CohereEmbeddingRequest, CohereEmbeddingResponse, CohereEmbeddingResponse> {

	public CohereEmbeddingBedrockApi(String modelId, String region) {
		super(modelId, region);
	}

	public CohereEmbeddingBedrockApi(String modelId, AwsCredentialsProvider credentialsProvider, String region,
			ObjectMapper objectMapper) {
		super(modelId, credentialsProvider, region, objectMapper);
	}

	public CohereEmbeddingBedrockApi(String modelId, String region, Duration timeout) {
		super(modelId, region, timeout);
	}

	public CohereEmbeddingBedrockApi(String modelId, AwsCredentialsProvider credentialsProvider, String region,
			ObjectMapper objectMapper, Duration timeout) {
		super(modelId, credentialsProvider, region, objectMapper, timeout);
	}

	public CohereEmbeddingBedrockApi(String modelId, AwsCredentialsProvider credentialsProvider, Region region,
			ObjectMapper objectMapper, Duration timeout) {
		super(modelId, credentialsProvider, region, objectMapper, timeout);
	}

	@Override
	public CohereEmbeddingResponse embedding(CohereEmbeddingRequest request) {
		return this.internalInvocation(request, CohereEmbeddingResponse.class);
	}

	public enum CohereEmbeddingModel {

		COHERE_EMBED_MULTILINGUAL_V3("cohere.embed-multilingual-v3"),

		COHERE_EMBED_ENGLISH_V3("cohere.embed-english-v3");

		private final String id;

		CohereEmbeddingModel(String value) {
			this.id = value;
		}

		public String id() {
			return this.id;
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record CohereEmbeddingRequest(@JsonProperty("texts") List<String> texts,
			@JsonProperty("input_type") InputType inputType, @JsonProperty("truncate") Truncate truncate) {

		public enum InputType {

			@JsonProperty("search_document")
			SEARCH_DOCUMENT,

			@JsonProperty("search_query")
			SEARCH_QUERY,

			@JsonProperty("classification")
			CLASSIFICATION,

			@JsonProperty("clustering")
			CLUSTERING

		}

		public enum Truncate {

			NONE,

			START,

			END

		}
	}

	@JsonInclude(Include.NON_NULL)
	public record CohereEmbeddingResponse(@JsonProperty("id") String id,
			@JsonProperty("embeddings") List<float[]> embeddings, @JsonProperty("texts") List<String> texts,
			@JsonProperty("response_type") String responseType,

			@JsonProperty("amazon-bedrock-invocationMetrics") AmazonBedrockInvocationMetrics amazonBedrockInvocationMetrics) {
	}

}
