package org.springframework.ai.minimax.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// @formatter:off

public class MiniMaxApi {

	public static final String DEFAULT_CHAT_MODEL = ChatModel.ABAB_6_5_G_Chat.getValue();
	public static final String DEFAULT_EMBEDDING_MODEL = EmbeddingModel.Embo_01.getValue();
	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final RestClient restClient;

	private final WebClient webClient;

	private final MiniMaxStreamFunctionCallingHelper chunkMerger = new MiniMaxStreamFunctionCallingHelper();

	public MiniMaxApi(String miniMaxToken) {
		this(MiniMaxApiConstants.DEFAULT_BASE_URL, miniMaxToken);
	}

	public MiniMaxApi(String baseUrl, String miniMaxToken) {
		this(baseUrl, miniMaxToken, RestClient.builder());
	}

	public MiniMaxApi(String baseUrl, String miniMaxToken, RestClient.Builder restClientBuilder) {
		this(baseUrl, miniMaxToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public MiniMaxApi(String baseUrl, String miniMaxToken, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> authHeaders = headers -> {
			headers.setBearerAuth(miniMaxToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};

		this.restClient = restClientBuilder
				.baseUrl(baseUrl)
				.defaultHeaders(authHeaders)
				.defaultStatusHandler(responseErrorHandler)
				.build();

		this.webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeaders(authHeaders)
				.build();
	}

	public static  String getTextContent(List<ChatCompletionMessage.MediaContent> content) {
		return content.stream()
				.filter(c -> "text".equals(c.type()))
				.map(ChatCompletionMessage.MediaContent::text)
				.reduce("", (a, b) -> a + b);
	}

	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");

		return this.restClient.post()
				.uri("/v1/text/chatcompletion_v2")
				.body(chatRequest)
				.retrieve()
				.toEntity(ChatCompletion.class);
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		return this.webClient.post()
				.uri("/v1/text/chatcompletion_v2")
				.body(Mono.just(chatRequest), ChatCompletionRequest.class)
				.retrieve()
				.bodyToFlux(String.class)
				.takeUntil(SSE_DONE_PREDICATE)
				.filter(SSE_DONE_PREDICATE.negate())
				.map(content -> ModelOptionsUtils.jsonToObject(content, ChatCompletionChunk.class))
				.map(chunk -> {
					if (this.chunkMerger.isStreamingToolFunctionCall(chunk)) {
						isInsideTool.set(true);
					}
					return chunk;
				})
				.windowUntil(chunk -> {
					if (isInsideTool.get() && this.chunkMerger.isStreamingToolFunctionCallFinish(chunk)) {
						isInsideTool.set(false);
						return true;
					}
					return !isInsideTool.get();
				})
				.concatMapIterable(window -> {
					Mono<ChatCompletionChunk> monoChunk = window.reduce(
							new ChatCompletionChunk(null, null, null, null, null, null),
							(previous, current) -> this.chunkMerger.merge(previous, current));
					return List.of(monoChunk);
				})
				.flatMap(mono -> mono);
	}

	public ResponseEntity<EmbeddingList> embeddings(EmbeddingRequest embeddingRequest) {

		Assert.notNull(embeddingRequest, "The request body can not be null.");

		Assert.notNull(embeddingRequest.texts(), "The input can not be null.");

		Assert.isTrue(!CollectionUtils.isEmpty(embeddingRequest.texts()), "The input list can not be empty.");

		return this.restClient.post()
				.uri("/v1/embeddings")
				.body(embeddingRequest)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>() {
		});
	}

	public enum ChatModel implements ChatModelDescription {
		MINIMAX_TEXT_01("minimax-text-01"),
		ABAB_7_Chat_Preview("abab7-chat-preview"),
		ABAB_6_5_Chat("abab6.5-chat"),
		ABAB_6_5_S_Chat("abab6.5s-chat"),
		ABAB_6_5_T_Chat("abab6.5t-chat"),
		ABAB_6_5_G_Chat("abab6.5g-chat"),
		ABAB_5_5_Chat("abab5.5-chat"),
		ABAB_5_5_S_Chat("abab5.5s-chat");

		public final String  value;

		ChatModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public String getName() {
			return this.value;
		}
	}

	public enum ChatCompletionFinishReason {

		@JsonProperty("stop")
		STOP,

		@JsonProperty("length")
		LENGTH,

		@JsonProperty("content_filter")
		CONTENT_FILTER,

		@JsonProperty("tool_calls")
		TOOL_CALLS,

		@JsonProperty("tool_call")
		TOOL_CALL
	}

	public enum EmbeddingModel {

		Embo_01("embo-01");

		public final String  value;

		EmbeddingModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum EmbeddingType {

		DB("db"),

		Query("query");

		@JsonValue
		public final String value;

		EmbeddingType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class FunctionTool {

		private Type type = Type.FUNCTION;

		private Function function;

		public FunctionTool() {

		}

		public FunctionTool(
				@JsonProperty("type") Type type,
				@JsonProperty("function") Function function) {
			this.type = type;
			this.function = function;
		}

		public FunctionTool(Function function) {
			this(Type.FUNCTION, function);
		}

		@JsonProperty("type")
		public Type getType() {
			return this.type;
		}

		@JsonProperty("function")
		public Function getFunction() {
			return this.function;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public void setFunction(Function function) {
			this.function = function;
		}

		public enum Type {

			@JsonProperty("function")
			FUNCTION,

			@JsonProperty("web_search")
			WEB_SEARCH
		}

		public static FunctionTool webSearchFunctionTool() {
			return new FunctionTool(FunctionTool.Type.WEB_SEARCH, null);
		}

		public static class Function {

			@JsonProperty("description")
			private String description;

			@JsonProperty("name")
			private String name;

			@JsonProperty("parameters")
			private Map<String, Object> parameters;

			@JsonIgnore
			private String jsonSchema;

			private Function() {

			}

			public Function(
					String description,
					String name,
					Map<String, Object> parameters) {
				this.description = description;
				this.name = name;
				this.parameters = parameters;
			}

			public Function(String description, String name, String jsonSchema) {
				this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema));
			}

			@JsonProperty("description")
			public String getDescription() {
				return this.description;
			}

			@JsonProperty("name")
			public String getName() {
				return this.name;
			}

			@JsonProperty("parameters")
			public Map<String, Object> getParameters() {
				return this.parameters;
			}

			public void setDescription(String description) {
				this.description = description;
			}

			public void setName(String name) {
				this.name = name;
			}

			public void setParameters(Map<String, Object> parameters) {
				this.parameters = parameters;
			}

			public String getJsonSchema() {
				return this.jsonSchema;
			}

			public void setJsonSchema(String jsonSchema) {
				this.jsonSchema = jsonSchema;
				if (jsonSchema != null) {
					this.parameters = ModelOptionsUtils.jsonToMap(jsonSchema);
				}
			}

		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("model") String model,
			@JsonProperty("frequency_penalty") Double frequencyPenalty,
			@JsonProperty("max_tokens") Integer maxTokens,
			@JsonProperty("n") Integer n,
			@JsonProperty("presence_penalty") Double presencePenalty,
			@JsonProperty("response_format") ResponseFormat responseFormat,
			@JsonProperty("seed") Integer seed,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP,
			@JsonProperty("mask_sensitive_info") Boolean maskSensitiveInfo,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("tool_choice") Object toolChoice) {

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
			this(messages, model, null,  null, null, null,
					null, null, null, false, temperature, null, null,
					null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature, boolean stream) {
			this(messages, model, null,  null, null, null,
					null, null, null, stream, temperature, null, null,
					null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model,
				List<FunctionTool> tools, Object toolChoice) {
			this(messages, model, null, null, null, null,
					null, null, null, false, 0.8, null, null,
					tools, toolChoice);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
			this(messages, null, null,  null, null, null,
					null, null, null, stream, null, null, null,
					null, null);
		}

		public static class ToolChoiceBuilder {

			public static final String AUTO = "auto";

			public static final String NONE = "none";

			public static Object function(String functionName) {
				return Map.of("type", "function", "function", Map.of("name", functionName));
			}
		}

		@JsonInclude(Include.NON_NULL)
		public record ResponseFormat(
				@JsonProperty("type") String type) {
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
			@JsonProperty("content") Object rawContent,
			@JsonProperty("role") Role role,
			@JsonProperty("name") String name,
			@JsonProperty("tool_call_id") String toolCallId,
			@JsonProperty("tool_calls") List<ToolCall> toolCalls) {

		public ChatCompletionMessage(Object content, Role role) {
			this(content, role, null, null, null);
		}

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
			ASSISTANT,

			@JsonProperty("tool")
			TOOL
		}

		@JsonInclude(Include.NON_NULL)
		public record MediaContent(
			@JsonProperty("type") String type,
			@JsonProperty("text") String text,
			@JsonProperty("image_url") ImageUrl imageUrl) {

			public MediaContent(String text) {
				this("text", text, null);
			}

			public MediaContent(ImageUrl imageUrl) {
				this("image_url", null, imageUrl);
			}

			@JsonInclude(Include.NON_NULL)
			public record ImageUrl(
				@JsonProperty("url") String url,
				@JsonProperty("detail") String detail) {

				public ImageUrl(String url) {
					this(url, null);
				}
			}
		}

		@JsonInclude(Include.NON_NULL)
		public record ToolCall(
				@JsonProperty("id") String id,
				@JsonProperty("type") String type,
				@JsonProperty("function") ChatCompletionFunction function) {
		}

		@JsonInclude(Include.NON_NULL)
		public record ChatCompletionFunction(
				@JsonProperty("name") String name,
				@JsonProperty("arguments") String arguments) {
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletion(
			@JsonProperty("id") String id,
			@JsonProperty("choices") List<Choice> choices,
			@JsonProperty("created") Long created,
			@JsonProperty("model") String model,
			@JsonProperty("system_fingerprint") String systemFingerprint,
			@JsonProperty("object") String object,

			@JsonProperty("base_resp") BaseResponse baseResponse,
			@JsonProperty("usage") Usage usage) {

		@JsonInclude(Include.NON_NULL)
		public record Choice(
				@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
				@JsonProperty("index") Integer index,
				@JsonProperty("message") ChatCompletionMessage message,
				@JsonProperty("messages") List<ChatCompletionMessage> messages,
				@JsonProperty("logprobs") LogProbs logprobs) {
		}

		public record BaseResponse(
				@JsonProperty("status_code") Long statusCode,
				@JsonProperty("status_msg") String message
		) { }
	}

	@JsonInclude(Include.NON_NULL)
	public record LogProbs(
			@JsonProperty("content") List<Content> content) {

		@JsonInclude(Include.NON_NULL)
		public record Content(
				@JsonProperty("token") String token,
				@JsonProperty("logprob") Float logprob,
				@JsonProperty("bytes") List<Integer> probBytes,
				@JsonProperty("top_logprobs") List<TopLogProbs> topLogprobs) {

			@JsonInclude(Include.NON_NULL)
			public record TopLogProbs(
					@JsonProperty("token") String token,
					@JsonProperty("logprob") Float logprob,
					@JsonProperty("bytes") List<Integer> probBytes) {
			}
		}
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
			@JsonProperty("choices") List<ChunkChoice> choices,
			@JsonProperty("created") Long created,
			@JsonProperty("model") String model,
			@JsonProperty("system_fingerprint") String systemFingerprint,
			@JsonProperty("object") String object) {

		@JsonInclude(Include.NON_NULL)
		public record ChunkChoice(
				@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
				@JsonProperty("index") Integer index,
				@JsonProperty("delta") ChatCompletionMessage delta,
				@JsonProperty("logprobs") LogProbs logprobs) {
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest(
			@JsonProperty("texts") List<String> texts,
			@JsonProperty("model") String model,
			@JsonProperty("type") String type
			) {

		public EmbeddingRequest(String text) {
			this(List.of(text), DEFAULT_EMBEDDING_MODEL, EmbeddingType.DB.value);
		}

		public EmbeddingRequest(String text, String model) {
			this(List.of(text), model, "db");
		}

		public EmbeddingRequest(String text, EmbeddingType type) {
			this(List.of(text), DEFAULT_EMBEDDING_MODEL, type.value);
		}

		public EmbeddingRequest(List<String> texts) {
			this(texts, DEFAULT_EMBEDDING_MODEL, EmbeddingType.DB.value);
		}

		public EmbeddingRequest(List<String> texts, String model) {
			this(texts, model, "db");
		}

		public EmbeddingRequest(List<String> texts, EmbeddingType type) {
			this(texts, DEFAULT_EMBEDDING_MODEL, type.value);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingList(
			@JsonProperty("vectors") List<float[]> vectors,
			@JsonProperty("model") String model,
			@JsonProperty("total_tokens") Integer totalTokens) {
	}

}
// @formatter:on
