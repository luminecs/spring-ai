package org.springframework.ai.qianfan.api;

import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.qianfan.api.auth.AuthApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// @formatter:off

public class QianFanApi extends AuthApi {

	public static final String DEFAULT_CHAT_MODEL = ChatModel.ERNIE_Speed_8K.getValue();
	public static final String DEFAULT_EMBEDDING_MODEL = EmbeddingModel.BGE_LARGE_ZH.getValue();
	private static final Predicate<ChatCompletionChunk> SSE_DONE_PREDICATE = ChatCompletionChunk::end;

	private final RestClient restClient;

	private final WebClient webClient;

	public QianFanApi(String apiKey, String secretKey) {
		this(QianFanConstants.DEFAULT_BASE_URL, apiKey, secretKey);
	}

	public QianFanApi(String baseUrl, String apiKey, String secretKey) {
		this(baseUrl, apiKey, secretKey, RestClient.builder());
	}

	public QianFanApi(String baseUrl, String apiKey, String secretKey, RestClient.Builder restClientBuilder) {
		this(baseUrl, apiKey, secretKey, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public QianFanApi(String baseUrl, String apiKey, String secretKey, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
		this(baseUrl, apiKey, secretKey, restClientBuilder, WebClient.builder(), responseErrorHandler);
	}

	public QianFanApi(String baseUrl, String apiKey, String secretKey, RestClient.Builder restClientBuilder,
					WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {
		super(apiKey, secretKey);

		this.restClient = restClientBuilder
				.baseUrl(baseUrl)
				.defaultHeaders(QianFanUtils.defaultHeaders())
				.defaultStatusHandler(responseErrorHandler)
				.build();

		this.webClient = webClientBuilder
				.baseUrl(baseUrl)
				.defaultHeaders(QianFanUtils.defaultHeaders())
				.build();
	}

	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");

		return this.restClient.post()
				.uri("/v1/wenxinworkshop/chat/{model}?access_token={token}", chatRequest.model, getAccessToken())
				.body(chatRequest)
				.retrieve()
				.toEntity(ChatCompletion.class);
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {
		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

		return this.webClient.post()
				.uri("/v1/wenxinworkshop/chat/{model}?access_token={token}", chatRequest.model, getAccessToken())
				.body(Mono.just(chatRequest), ChatCompletionRequest.class)
				.retrieve()
				.bodyToFlux(ChatCompletionChunk.class)
				.takeUntil(SSE_DONE_PREDICATE);
	}

	public ResponseEntity<EmbeddingList> embeddings(EmbeddingRequest embeddingRequest) {

		Assert.notNull(embeddingRequest, "The request body can not be null.");

		Assert.notNull(embeddingRequest.texts(), "The input can not be null.");

		Assert.isTrue(!CollectionUtils.isEmpty(embeddingRequest.texts()), "The input list can not be empty.");
		Assert.isTrue(embeddingRequest.texts().size() <= 16, "The list must be 16 dimensions or less");

		return this.restClient.post()
				.uri("/v1/wenxinworkshop/embeddings/{model}?access_token={token}", embeddingRequest.model, getAccessToken())
				.body(embeddingRequest)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>() {

				});
	}

	public enum ChatModel {
		ERNIE_4_0_8K("completions_pro"),
		ERNIE_4_0_8K_Preview("ernie-4.0-8k-preview"),
		ERNIE_4_0_8K_Preview_0518("completions_adv_pro"),
		ERNIE_4_0_8K_0329("ernie-4.0-8k-0329"),
		ERNIE_4_0_8K_0104("ernie-4.0-8k-0104"),
		ERNIE_3_5_8K("completions"),
		ERNIE_3_5_128K("ernie-3.5-128k"),
		ERNIE_3_5_8K_Preview("ernie-3.5-8k-preview"),
		ERNIE_3_5_8K_0205("ernie-3.5-8k-0205"),
		ERNIE_3_5_8K_0329("ernie-3.5-8k-0329"),
		ERNIE_3_5_8K_1222("ernie-3.5-8k-1222"),
		ERNIE_3_5_4K_0205("ernie-3.5-4k-0205"),

		ERNIE_Lite_8K_0922("eb-instant"),
		ERNIE_Lite_8K_0308("ernie-lite-8k"),
		ERNIE_Speed_8K("ernie_speed"),
		ERNIE_Speed_128K("ernie-speed-128k"),
		ERNIE_Tiny_8K("ernie-tiny-8k"),
		ERNIE_FUNC_8K("ernie-func-8k");

		public final String  value;

		ChatModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum EmbeddingModel {

		EMBEDDING_V1("embedding-v1"),

		BGE_LARGE_ZH("bge_large_zh"),

		BGE_LARGE_EN("bge_large_en"),

		TAO_8K("tao_8k");

		public final String  value;

		EmbeddingModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("system") String system,
			@JsonProperty("model") String model,
			@JsonProperty("frequency_penalty") Double frequencyPenalty,
			@JsonProperty("max_output_tokens") Integer maxTokens,
			@JsonProperty("presence_penalty") Double presencePenalty,
			@JsonProperty("response_format") ResponseFormat responseFormat,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP) {

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String system, String model, Double temperature) {
			this(messages, system, model, null, null,
					null, null, null, false, temperature, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String system, String model, Double temperature, boolean stream) {
			this(messages, system, model, null, null,
					null, null, null, stream, temperature, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String system, Boolean stream) {
			this(messages, system, DEFAULT_CHAT_MODEL, null, null,
					null, null, null, stream, 0.8, null);
		}

		@JsonInclude(Include.NON_NULL)
		public record ResponseFormat(
				@JsonProperty("type") String type) {
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
			@JsonProperty("content") Object rawContent,
			@JsonProperty("role") Role role) {

		public String content() {
			if (this.rawContent == null) {
				return null;
			}
			if (this.rawContent instanceof String text) {
				return text;
			}
			throw new IllegalStateException("The content is not a string!");
		}

		public enum Role {

			@JsonProperty("system")
			SYSTEM,

			@JsonProperty("user")
			USER,

			@JsonProperty("assistant")
			ASSISTANT
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletion(
			@JsonProperty("id") String id,
			@JsonProperty("object") String object,
			@JsonProperty("created") Long created,
			@JsonProperty("result") String result,
			@JsonProperty("finish_reason") String finishReason,
			@JsonProperty("usage") Usage usage) {
	}

	@JsonInclude(Include.NON_NULL)
	public record Usage(
			@JsonProperty("completion_tokens") Integer completionTokens,
			@JsonProperty("prompt_tokens") Integer promptTokens,
			@JsonProperty("total_tokens") Integer totalTokens) {

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionChunk(
			@JsonProperty("id") String id,
			@JsonProperty("object") String object,
			@JsonProperty("created") Long created,
			@JsonProperty("result") String result,
			@JsonProperty("finish_reason") String finishReason,
			@JsonProperty("is_end") Boolean end,

			@JsonProperty("usage") Usage usage
			) {
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest(
			@JsonProperty("input") List<String> texts,
			@JsonProperty("model") String model,
			@JsonProperty("user_id") String user
			) {

		public EmbeddingRequest(String text) {
			this(List.of(text), DEFAULT_EMBEDDING_MODEL, null);
		}

		public EmbeddingRequest(String text, String model, String userId) {
			this(List.of(text), model, userId);
		}

		public EmbeddingRequest(List<String> texts) {
			this(texts, DEFAULT_EMBEDDING_MODEL, null);
		}

		public EmbeddingRequest(List<String> texts, String model) {
			this(texts, model, null);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record Embedding(
			// @formatter:off
			@JsonProperty("index") Integer index,
			@JsonProperty("embedding") float[] embedding,
			@JsonProperty("object") String object) {
		// @formatter:on

		public Embedding(Integer index, float[] embedding) {
			this(index, embedding, "embedding");
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingList(
	// @formatter:off
			@JsonProperty("object") String object,
			@JsonProperty("data") List<Embedding> data,
			@JsonProperty("model") String model,
			@JsonProperty("error_code") String errorCode,
			@JsonProperty("error_msg") String errorNsg,
			@JsonProperty("usage") Usage usage) {
		// @formatter:on
	}

}
// @formatter:on
