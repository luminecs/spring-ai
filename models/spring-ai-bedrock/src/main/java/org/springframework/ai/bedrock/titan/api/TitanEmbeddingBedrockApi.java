package org.springframework.ai.bedrock.titan.api;

import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import org.springframework.ai.bedrock.api.AbstractBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingRequest;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingResponse;
import org.springframework.util.Assert;

// @formatter:off
public class TitanEmbeddingBedrockApi extends
		AbstractBedrockApi<TitanEmbeddingRequest, TitanEmbeddingResponse, TitanEmbeddingResponse> {

	public TitanEmbeddingBedrockApi(String modelId, String region, Duration timeout) {
		super(modelId, region, timeout);
	}

	public TitanEmbeddingBedrockApi(String modelId, AwsCredentialsProvider credentialsProvider, String region,
			ObjectMapper objectMapper, Duration timeout) {
		super(modelId, credentialsProvider, region, objectMapper, timeout);
	}

	public TitanEmbeddingBedrockApi(String modelId, AwsCredentialsProvider credentialsProvider, Region region,
			ObjectMapper objectMapper, Duration timeout) {
		super(modelId, credentialsProvider, region, objectMapper, timeout);
	}

	@Override
	public TitanEmbeddingResponse embedding(TitanEmbeddingRequest request) {
		return this.internalInvocation(request, TitanEmbeddingResponse.class);
	}

	public enum TitanEmbeddingModel {

		TITAN_EMBED_IMAGE_V1("amazon.titan-embed-image-v1"),

		TITAN_EMBED_TEXT_V1("amazon.titan-embed-text-v1"),

		TITAN_EMBED_TEXT_V2("amazon.titan-embed-text-v2:0");

		private final String id;

		TitanEmbeddingModel(String value) {
			this.id = value;
		}

		public String id() {
			return this.id;
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record TitanEmbeddingRequest(
			@JsonProperty("inputText") String inputText,
			@JsonProperty("inputImage") String inputImage) {

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private String inputText;
			private String inputImage;

			public Builder inputText(String inputText) {
				this.inputText = inputText;
				return this;
			}

			public Builder inputImage(String inputImage) {
				this.inputImage = inputImage;
				return this;
			}

			public TitanEmbeddingRequest build() {
				Assert.isTrue(this.inputText != null || this.inputImage != null,
						"At least one of the inputText or inputImage parameters must be provided!");
				Assert.isTrue(!(this.inputText != null && this.inputImage != null),
						"Only one of the inputText or inputImage parameters must be provided!");

				return new TitanEmbeddingRequest(this.inputText, this.inputImage);
			}
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record TitanEmbeddingResponse(
			@JsonProperty("embedding") float[] embedding,
			@JsonProperty("inputTextTokenCount") Integer inputTextTokenCount,
			@JsonProperty("successCount") Integer successCount,
			@JsonProperty("failureCount") Integer failureCount,
			@JsonProperty("embeddingsByType") Map<String, Object> embeddingsByType,
			@JsonProperty("results") Object results,
			@JsonProperty("message") Object message) {

	}
}
// @formatter:on
