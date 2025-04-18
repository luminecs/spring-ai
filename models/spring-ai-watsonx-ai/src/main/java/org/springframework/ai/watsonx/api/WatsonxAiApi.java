package org.springframework.ai.watsonx.api;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.security.IamToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// @formatter:off
public class WatsonxAiApi {

	public static final String WATSONX_REQUEST_CANNOT_BE_NULL = "Watsonx Request cannot be null";

	private static final Log logger = LogFactory.getLog(WatsonxAiApi.class);

	private final RestClient restClient;
	private final WebClient webClient;
	private final IamAuthenticator iamAuthenticator;
	private final String streamEndpoint;
	private final String textEndpoint;
	private final String embeddingEndpoint;
	private final String projectId;
	private IamToken token;

	public WatsonxAiApi(
			String baseUrl,
			String streamEndpoint,
			String textEndpoint,
			String embeddingEndpoint,
			String projectId,
			String IAMToken,
			RestClient.Builder restClientBuilder
	) {
		this.streamEndpoint = streamEndpoint;
		this.textEndpoint = textEndpoint;
		this.embeddingEndpoint = embeddingEndpoint;
		this.projectId = projectId;
		this.iamAuthenticator = IamAuthenticator.fromConfiguration(Map.of("APIKEY", IAMToken));
		this.token = this.iamAuthenticator.requestToken();

		Consumer<HttpHeaders> defaultHeaders = headers -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultStatusHandler(RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER)
			.defaultHeaders(defaultHeaders)
			.build();

		this.webClient = WebClient.builder().baseUrl(baseUrl)
			.defaultHeaders(defaultHeaders)
			.build();
	}

	@Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(random = true, delay = 1200, maxDelay = 7000, multiplier = 2.5))
	public ResponseEntity<WatsonxAiChatResponse> generate(WatsonxAiChatRequest watsonxAiChatRequest) {
		Assert.notNull(watsonxAiChatRequest, WATSONX_REQUEST_CANNOT_BE_NULL);

		if (this.token.needsRefresh()) {
			this.token = this.iamAuthenticator.requestToken();
		}

		return this.restClient.post()
				.uri(this.textEndpoint)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + this.token.getAccessToken())
				.body(watsonxAiChatRequest.withProjectId(this.projectId))
				.retrieve()
				.toEntity(WatsonxAiChatResponse.class);
	}

	@Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(random = true, delay = 1200, maxDelay = 7000, multiplier = 2.5))
	public Flux<WatsonxAiChatResponse> generateStreaming(WatsonxAiChatRequest watsonxAiChatRequest) {
		Assert.notNull(watsonxAiChatRequest, WATSONX_REQUEST_CANNOT_BE_NULL);

		if (this.token.needsRefresh()) {
			this.token = this.iamAuthenticator.requestToken();
		}

		return this.webClient.post()
				.uri(this.streamEndpoint)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + this.token.getAccessToken())
				.bodyValue(watsonxAiChatRequest.withProjectId(this.projectId))
				.retrieve()
				.bodyToFlux(WatsonxAiChatResponse.class)
				.handle((data, sink) -> {
					if (logger.isTraceEnabled()) {
						logger.trace(data);
					}
					sink.next(data);
				});
		}

	@Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(random = true, delay = 1200, maxDelay = 7000, multiplier = 2.5))
	public ResponseEntity<WatsonxAiEmbeddingResponse> embeddings(WatsonxAiEmbeddingRequest request) {
		Assert.notNull(request, WATSONX_REQUEST_CANNOT_BE_NULL);

		if (this.token.needsRefresh()) {
			this.token = this.iamAuthenticator.requestToken();
		}

		return this.restClient.post()
				.uri(this.embeddingEndpoint)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + this.token.getAccessToken())
				.body(request.withProjectId(this.projectId))
				.retrieve()
				.toEntity(WatsonxAiEmbeddingResponse.class);
	}

}
