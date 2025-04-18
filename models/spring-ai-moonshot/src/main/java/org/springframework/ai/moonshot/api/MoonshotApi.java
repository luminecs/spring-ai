package org.springframework.ai.moonshot.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class MoonshotApi {

	public static final String DEFAULT_CHAT_MODEL = ChatModel.MOONSHOT_V1_8K.getValue();

	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final RestClient restClient;

	private final WebClient webClient;

	private final MoonshotStreamFunctionCallingHelper chunkMerger = new MoonshotStreamFunctionCallingHelper();

	public MoonshotApi(String moonshotApiKey) {
		this(org.springframework.ai.moonshot.api.MoonshotConstants.DEFAULT_BASE_URL, moonshotApiKey);
	}

	public MoonshotApi(String baseUrl, String moonshotApiKey) {
		this(baseUrl, moonshotApiKey, RestClient.builder(), RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public MoonshotApi(String baseUrl, String moonshotApiKey, RestClient.Builder restClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.setBearerAuth(moonshotApiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(jsonContentHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();

		this.webClient = WebClient.builder().baseUrl(baseUrl).defaultHeaders(jsonContentHeaders).build();
	}

	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");

		return this.restClient.post()
			.uri("/v1/chat/completions")
			.body(chatRequest)
			.retrieve()
			.toEntity(ChatCompletion.class);
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {
		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");
		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		return this.webClient.post()
			.uri("/v1/chat/completions")
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
						new ChatCompletionChunk(null, null, null, null, null),
						(previous, current) -> this.chunkMerger.merge(previous, current));
				return List.of(monoChunk);
			})

			.flatMap(mono -> mono);
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

	public enum ChatModel implements ChatModelDescription {

		// @formatter:off
		MOONSHOT_V1_8K("moonshot-v1-8k"),
		MOONSHOT_V1_32K("moonshot-v1-32k"),
		MOONSHOT_V1_128K("moonshot-v1-128k");
		 // @formatter:on

		private final String value;

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

	@JsonInclude(Include.NON_NULL)
	public record Usage(
	// @formatter:off
		@JsonProperty("prompt_tokens") Integer promptTokens,
		@JsonProperty("total_tokens") Integer totalTokens,
		@JsonProperty("completion_tokens") Integer completionTokens) {
		// @formatter:on
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
	// @formatter:off
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("model") String model,
			@JsonProperty("max_tokens") Integer maxTokens,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP,
			@JsonProperty("n") Integer n,
			@JsonProperty("frequency_penalty") Double frequencyPenalty,
			@JsonProperty("presence_penalty") Double presencePenalty,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("tool_choice") Object toolChoice) {
		 // @formatter:on

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model) {
			this(messages, model, null, 0.3, 1.0, null, null, null, null, false, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature,
				boolean stream) {
			this(messages, model, null, temperature, 1.0, null, null, null, null, stream, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
			this(messages, model, null, temperature, 1.0, null, null, null, null, false, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, List<FunctionTool> tools,
				Object toolChoice) {
			this(messages, model, null, null, 1.0, null, null, null, null, false, tools, toolChoice);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
			this(messages, DEFAULT_CHAT_MODEL, null, 0.7, 1.0, null, null, null, null, stream, null, null);
		}

		public static class ToolChoiceBuilder {

			public static final String AUTO = "auto";

			public static final String NONE = "none";

			public static Object function(String functionName) {
				return Map.of("type", "function", "function", Map.of("name", functionName));
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
	// @formatter:off
		@JsonProperty("content") Object rawContent,
		@JsonProperty("role") Role role,
		@JsonProperty("name") String name,
		@JsonProperty("tool_call_id") String toolCallId,
		@JsonProperty("tool_calls") List<ToolCall> toolCalls
	// @formatter:on
	) {

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
			// @formatter:on

		}

		@JsonInclude(Include.NON_NULL)
		public record ToolCall(@JsonProperty("id") String id, @JsonProperty("type") String type,
				@JsonProperty("function") ChatCompletionFunction function) {

		}

		@JsonInclude(Include.NON_NULL)
		public record ChatCompletionFunction(@JsonProperty("name") String name,
				@JsonProperty("arguments") String arguments) {

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletion(
	// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("object") String object,
		@JsonProperty("created") Long created,
		@JsonProperty("model") String model,
		@JsonProperty("choices") List<Choice> choices,
		@JsonProperty("usage") Usage usage) {
		 // @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record Choice(
		// @formatter:off
			@JsonProperty("index") Integer index,
			@JsonProperty("message") ChatCompletionMessage message,
			@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
			@JsonProperty("usage") Usage usage) {
			 // @formatter:on
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionChunk(
	// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("object") String object,
		@JsonProperty("created") Long created,
		@JsonProperty("model") String model,
		@JsonProperty("choices") List<ChunkChoice> choices) {
		 // @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record ChunkChoice(
		// @formatter:off
			@JsonProperty("index") Integer index,
			@JsonProperty("delta") ChatCompletionMessage delta,
			@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
			@JsonProperty("usage") Usage usage
		// @formatter:on
		) {

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record FunctionTool(@JsonProperty("type") Type type, @JsonProperty("function") Function function) {

		public FunctionTool(Function function) {
			this(Type.FUNCTION, function);
		}

		public enum Type {

			@JsonProperty("function")
			FUNCTION

		}

		public record Function(@JsonProperty("description") String description, @JsonProperty("name") String name,
				@JsonProperty("parameters") Map<String, Object> parameters) {

			public Function(String description, String name, String jsonSchema) {
				this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema));
			}

		}

	}

}
