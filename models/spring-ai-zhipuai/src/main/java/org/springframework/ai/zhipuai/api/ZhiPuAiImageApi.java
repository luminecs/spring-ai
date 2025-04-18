package org.springframework.ai.zhipuai.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

public class ZhiPuAiImageApi {

	public static final String DEFAULT_IMAGE_MODEL = ImageModel.CogView_3.getValue();

	private final RestClient restClient;

	public ZhiPuAiImageApi(String zhiPuAiToken) {
		this(ZhiPuApiConstants.DEFAULT_BASE_URL, zhiPuAiToken, RestClient.builder());
	}

	public ZhiPuAiImageApi(String baseUrl, String zhiPuAiToken, RestClient.Builder restClientBuilder) {
		this(baseUrl, zhiPuAiToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public ZhiPuAiImageApi(String baseUrl, String zhiPuAiToken, RestClient.Builder restClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		this.restClient = restClientBuilder.baseUrl(baseUrl).defaultHeaders(h -> h.setBearerAuth(zhiPuAiToken)

		).defaultStatusHandler(responseErrorHandler).build();
	}

	public ResponseEntity<ZhiPuAiImageResponse> createImage(ZhiPuAiImageRequest zhiPuAiImageRequest) {
		Assert.notNull(zhiPuAiImageRequest, "Image request cannot be null.");
		Assert.hasLength(zhiPuAiImageRequest.prompt(), "Prompt cannot be empty.");

		return this.restClient.post()
			.uri("/v4/images/generations")
			.body(zhiPuAiImageRequest)
			.retrieve()
			.toEntity(ZhiPuAiImageResponse.class);
	}

	public enum ImageModel {

		CogView_3("cogview-3");

		private final String value;

		ImageModel(String model) {
			this.value = model;
		}

		public String getValue() {
			return this.value;
		}

	}

	// @formatter:off
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ZhiPuAiImageRequest(
		@JsonProperty("prompt") String prompt,
		@JsonProperty("model") String model,
		@JsonProperty("user_id") String user) {

		public ZhiPuAiImageRequest(String prompt, String model) {
			this(prompt, model, null);
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ZhiPuAiImageResponse(
		@JsonProperty("created") Long created,
		@JsonProperty("data") List<Data> data) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Data(@JsonProperty("url") String url) {

	}

}
