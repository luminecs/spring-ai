package org.springframework.ai.anthropic.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.anthropic.api.StreamHelper.ChatCompletionResponseBuilder;
import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class AnthropicApi {

	public static final String PROVIDER_NAME = AiProvider.ANTHROPIC.value();

	public static final String DEFAULT_BASE_URL = "https://api.anthropic.com";

	public static final String DEFAULT_ANTHROPIC_VERSION = "2023-06-01";

	public static final String DEFAULT_ANTHROPIC_BETA_VERSION = "tools-2024-04-04,pdfs-2024-09-25";

	public static final String BETA_MAX_TOKENS = "max-tokens-3-5-sonnet-2024-07-15";

	private static final String HEADER_X_API_KEY = "x-api-key";

	private static final String HEADER_ANTHROPIC_VERSION = "anthropic-version";

	private static final String HEADER_ANTHROPIC_BETA = "anthropic-beta";

	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final RestClient restClient;

	private final StreamHelper streamHelper = new StreamHelper();

	private final WebClient webClient;

	public AnthropicApi(String anthropicApiKey) {
		this(DEFAULT_BASE_URL, anthropicApiKey);
	}

	public AnthropicApi(String baseUrl, String anthropicApiKey) {
		this(baseUrl, anthropicApiKey, DEFAULT_ANTHROPIC_VERSION, RestClient.builder(), WebClient.builder(),
				RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public AnthropicApi(String baseUrl, String anthropicApiKey, String anthropicVersion,
			RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
			ResponseErrorHandler responseErrorHandler) {
		this(baseUrl, anthropicApiKey, anthropicVersion, restClientBuilder, webClientBuilder, responseErrorHandler,
				DEFAULT_ANTHROPIC_BETA_VERSION);
	}

	public AnthropicApi(String baseUrl, String anthropicApiKey, String anthropicVersion,
			RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
			ResponseErrorHandler responseErrorHandler, String anthropicBetaFeatures) {

		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.add(HEADER_X_API_KEY, anthropicApiKey);
			headers.add(HEADER_ANTHROPIC_VERSION, anthropicVersion);
			headers.add(HEADER_ANTHROPIC_BETA, anthropicBetaFeatures);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(jsonContentHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();

		this.webClient = webClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(jsonContentHeaders)
			.defaultStatusHandler(HttpStatusCode::isError,
					resp -> resp.bodyToMono(String.class)
						.flatMap(it -> Mono.error(new RuntimeException(
								"Response exception, Status: [" + resp.statusCode() + "], Body:[" + it + "]"))))
			.build();
	}

	public ResponseEntity<ChatCompletionResponse> chatCompletionEntity(ChatCompletionRequest chatRequest) {
		return chatCompletionEntity(chatRequest, new LinkedMultiValueMap<>());
	}

	public ResponseEntity<ChatCompletionResponse> chatCompletionEntity(ChatCompletionRequest chatRequest,
			MultiValueMap<String, String> additionalHttpHeader) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");
		Assert.notNull(additionalHttpHeader, "The additional HTTP headers can not be null.");

		return this.restClient.post()
			.uri("/v1/messages")
			.headers(headers -> headers.addAll(additionalHttpHeader))
			.body(chatRequest)
			.retrieve()
			.toEntity(ChatCompletionResponse.class);
	}

	public Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest chatRequest) {
		return chatCompletionStream(chatRequest, new LinkedMultiValueMap<>());
	}

	public Flux<ChatCompletionResponse> chatCompletionStream(ChatCompletionRequest chatRequest,
			MultiValueMap<String, String> additionalHttpHeader) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");
		Assert.notNull(additionalHttpHeader, "The additional HTTP headers can not be null.");

		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		AtomicReference<ChatCompletionResponseBuilder> chatCompletionReference = new AtomicReference<>();

		return this.webClient.post()
			.uri("/v1/messages")
			.headers(headers -> headers.addAll(additionalHttpHeader))
			.body(Mono.just(chatRequest), ChatCompletionRequest.class)
			.retrieve()
			.bodyToFlux(String.class)
			.takeUntil(SSE_DONE_PREDICATE)
			.filter(SSE_DONE_PREDICATE.negate())
			.map(content -> ModelOptionsUtils.jsonToObject(content, StreamEvent.class))
			.filter(event -> event.type() != EventType.PING)

			.map(event -> {
				if (this.streamHelper.isToolUseStart(event)) {
					isInsideTool.set(true);
				}
				return event;
			})

			.windowUntil(event -> {
				if (isInsideTool.get() && this.streamHelper.isToolUseFinish(event)) {
					isInsideTool.set(false);
					return true;
				}
				return !isInsideTool.get();
			})

			.concatMapIterable(window -> {
				Mono<StreamEvent> monoChunk = window.reduce(new ToolUseAggregationEvent(),
						this.streamHelper::mergeToolUseEvents);
				return List.of(monoChunk);
			})
			.flatMap(mono -> mono)
			.map(event -> this.streamHelper.eventToChatCompletionResponse(event, chatCompletionReference))
			.filter(chatCompletionResponse -> chatCompletionResponse.type() != null);
	}

	public enum ChatModel implements ChatModelDescription {

		// @formatter:off

		CLAUDE_3_7_SONNET("claude-3-7-sonnet-latest"),

		CLAUDE_3_5_SONNET("claude-3-5-sonnet-latest"),

		CLAUDE_3_OPUS("claude-3-opus-latest"),

		CLAUDE_3_SONNET("claude-3-sonnet-20240229"),

		CLAUDE_3_5_HAIKU("claude-3-5-haiku-latest"),

		CLAUDE_3_HAIKU("claude-3-haiku-20240307"),

		CLAUDE_2_1("claude-2.1"),

		CLAUDE_2("claude-2.0");

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

	public enum Role {

		// @formatter:off

		@JsonProperty("user")
		USER,

		@JsonProperty("assistant")
		ASSISTANT
		// @formatter:on

	}

	public enum ThinkingType {

		@JsonProperty("enabled")
		ENABLED,

		@JsonProperty("disabled")
		DISABLED

	}

	public enum EventType {

		@JsonProperty("message_start")
		MESSAGE_START,

		@JsonProperty("message_delta")
		MESSAGE_DELTA,

		@JsonProperty("message_stop")
		MESSAGE_STOP,

		@JsonProperty("content_block_start")
		CONTENT_BLOCK_START,

		@JsonProperty("content_block_delta")
		CONTENT_BLOCK_DELTA,

		@JsonProperty("content_block_stop")
		CONTENT_BLOCK_STOP,

		@JsonProperty("error")
		ERROR,

		@JsonProperty("ping")
		PING,

		TOOL_USE_AGGREGATE

	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
			visible = true)
	@JsonSubTypes({ @JsonSubTypes.Type(value = ContentBlockStartEvent.class, name = "content_block_start"),
			@JsonSubTypes.Type(value = ContentBlockDeltaEvent.class, name = "content_block_delta"),
			@JsonSubTypes.Type(value = ContentBlockStopEvent.class, name = "content_block_stop"),
			@JsonSubTypes.Type(value = PingEvent.class, name = "ping"),
			@JsonSubTypes.Type(value = ErrorEvent.class, name = "error"),
			@JsonSubTypes.Type(value = MessageStartEvent.class, name = "message_start"),
			@JsonSubTypes.Type(value = MessageDeltaEvent.class, name = "message_delta"),
			@JsonSubTypes.Type(value = MessageStopEvent.class, name = "message_stop") })
	public interface StreamEvent {

		@JsonProperty("type")
		EventType type();

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
	// @formatter:off
		@JsonProperty("model") String model,
		@JsonProperty("messages") List<AnthropicMessage> messages,
		@JsonProperty("system") String system,
		@JsonProperty("max_tokens") Integer maxTokens,
		@JsonProperty("metadata") Metadata metadata,
		@JsonProperty("stop_sequences") List<String> stopSequences,
		@JsonProperty("stream") Boolean stream,
		@JsonProperty("temperature") Double temperature,
		@JsonProperty("top_p") Double topP,
		@JsonProperty("top_k") Integer topK,
		@JsonProperty("tools") List<Tool> tools,
		@JsonProperty("thinking") ThinkingConfig thinking) {
		// @formatter:on

		public ChatCompletionRequest(String model, List<AnthropicMessage> messages, String system, Integer maxTokens,
				Double temperature, Boolean stream) {
			this(model, messages, system, maxTokens, null, null, stream, temperature, null, null, null, null);
		}

		public ChatCompletionRequest(String model, List<AnthropicMessage> messages, String system, Integer maxTokens,
				List<String> stopSequences, Double temperature, Boolean stream) {
			this(model, messages, system, maxTokens, null, stopSequences, stream, temperature, null, null, null, null);
		}

		public static ChatCompletionRequestBuilder builder() {
			return new ChatCompletionRequestBuilder();
		}

		public static ChatCompletionRequestBuilder from(ChatCompletionRequest request) {
			return new ChatCompletionRequestBuilder(request);
		}

		@JsonInclude(Include.NON_NULL)
		public record Metadata(@JsonProperty("user_id") String userId) {

		}

		@JsonInclude(Include.NON_NULL)
		public record ThinkingConfig(@JsonProperty("type") ThinkingType type,
				@JsonProperty("budget_tokens") Integer budgetTokens) {
		}

	}

	public static final class ChatCompletionRequestBuilder {

		private String model;

		private List<AnthropicMessage> messages;

		private String system;

		private Integer maxTokens;

		private ChatCompletionRequest.Metadata metadata;

		private List<String> stopSequences;

		private Boolean stream = false;

		private Double temperature;

		private Double topP;

		private Integer topK;

		private List<Tool> tools;

		private ChatCompletionRequest.ThinkingConfig thinking;

		private ChatCompletionRequestBuilder() {
		}

		private ChatCompletionRequestBuilder(ChatCompletionRequest request) {
			this.model = request.model;
			this.messages = request.messages;
			this.system = request.system;
			this.maxTokens = request.maxTokens;
			this.metadata = request.metadata;
			this.stopSequences = request.stopSequences;
			this.stream = request.stream;
			this.temperature = request.temperature;
			this.topP = request.topP;
			this.topK = request.topK;
			this.tools = request.tools;
			this.thinking = request.thinking;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withModel(ChatModel model) {
			this.model = model.getValue();
			return this;
		}

		public ChatCompletionRequestBuilder model(ChatModel model) {
			this.model = model.getValue();
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withModel(String model) {
			this.model = model;
			return this;
		}

		public ChatCompletionRequestBuilder model(String model) {
			this.model = model;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withMessages(List<AnthropicMessage> messages) {
			this.messages = messages;
			return this;
		}

		public ChatCompletionRequestBuilder messages(List<AnthropicMessage> messages) {
			this.messages = messages;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withSystem(String system) {
			this.system = system;
			return this;
		}

		public ChatCompletionRequestBuilder system(String system) {
			this.system = system;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withMaxTokens(Integer maxTokens) {
			this.maxTokens = maxTokens;
			return this;
		}

		public ChatCompletionRequestBuilder maxTokens(Integer maxTokens) {
			this.maxTokens = maxTokens;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withMetadata(ChatCompletionRequest.Metadata metadata) {
			this.metadata = metadata;
			return this;
		}

		public ChatCompletionRequestBuilder metadata(ChatCompletionRequest.Metadata metadata) {
			this.metadata = metadata;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withStopSequences(List<String> stopSequences) {
			this.stopSequences = stopSequences;
			return this;
		}

		public ChatCompletionRequestBuilder stopSequences(List<String> stopSequences) {
			this.stopSequences = stopSequences;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withStream(Boolean stream) {
			this.stream = stream;
			return this;
		}

		public ChatCompletionRequestBuilder stream(Boolean stream) {
			this.stream = stream;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withTemperature(Double temperature) {
			this.temperature = temperature;
			return this;
		}

		public ChatCompletionRequestBuilder temperature(Double temperature) {
			this.temperature = temperature;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withTopP(Double topP) {
			this.topP = topP;
			return this;
		}

		public ChatCompletionRequestBuilder topP(Double topP) {
			this.topP = topP;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withTopK(Integer topK) {
			this.topK = topK;
			return this;
		}

		public ChatCompletionRequestBuilder topK(Integer topK) {
			this.topK = topK;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withTools(List<Tool> tools) {
			this.tools = tools;
			return this;
		}

		public ChatCompletionRequestBuilder tools(List<Tool> tools) {
			this.tools = tools;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withThinking(ChatCompletionRequest.ThinkingConfig thinking) {
			this.thinking = thinking;
			return this;
		}

		public ChatCompletionRequestBuilder thinking(ChatCompletionRequest.ThinkingConfig thinking) {
			this.thinking = thinking;
			return this;
		}

		@Deprecated(forRemoval = true, since = "1.0.0-M6")
		public ChatCompletionRequestBuilder withThinking(ThinkingType type, Integer budgetTokens) {
			this.thinking = new ChatCompletionRequest.ThinkingConfig(type, budgetTokens);
			return this;
		}

		public ChatCompletionRequestBuilder thinking(ThinkingType type, Integer budgetTokens) {
			this.thinking = new ChatCompletionRequest.ThinkingConfig(type, budgetTokens);
			return this;
		}

		public ChatCompletionRequest build() {
			return new ChatCompletionRequest(this.model, this.messages, this.system, this.maxTokens, this.metadata,
					this.stopSequences, this.stream, this.temperature, this.topP, this.topK, this.tools, this.thinking);
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record AnthropicMessage(
	// @formatter:off
		@JsonProperty("content") List<ContentBlock> content,
		@JsonProperty("role") Role role) {
		// @formatter:on
	}

	@JsonInclude(Include.NON_NULL)
	public record ContentBlock(
	// @formatter:off
		@JsonProperty("type") Type type,
		@JsonProperty("source") Source source,
		@JsonProperty("text") String text,

		@JsonProperty("index") Integer index,

		@JsonProperty("id") String id,
		@JsonProperty("name") String name,
		@JsonProperty("input") Map<String, Object> input,

		@JsonProperty("tool_use_id") String toolUseId,
		@JsonProperty("content") String content,

		@JsonProperty("signature") String signature,
		@JsonProperty("thinking") String thinking,

		@JsonProperty("data") String data
		) {
		// @formatter:on

		public ContentBlock(String mediaType, String data) {
			this(new Source(mediaType, data));
		}

		public ContentBlock(Type type, Source source) {
			this(type, source, null, null, null, null, null, null, null, null, null, null);
		}

		public ContentBlock(Source source) {
			this(Type.IMAGE, source, null, null, null, null, null, null, null, null, null, null);
		}

		public ContentBlock(String text) {
			this(Type.TEXT, null, text, null, null, null, null, null, null, null, null, null);
		}

		public ContentBlock(Type type, String toolUseId, String content) {
			this(type, null, null, null, null, null, null, toolUseId, content, null, null, null);
		}

		public ContentBlock(Type type, Source source, String text, Integer index) {
			this(type, source, text, index, null, null, null, null, null, null, null, null);
		}

		public ContentBlock(Type type, String id, String name, Map<String, Object> input) {
			this(type, null, null, null, id, name, input, null, null, null, null, null);
		}

		public enum Type {

			@JsonProperty("tool_use")
			TOOL_USE("tool_use"),

			@JsonProperty("tool_result")
			TOOL_RESULT("tool_result"),

			@JsonProperty("text")
			TEXT("text"),

			@JsonProperty("text_delta")
			TEXT_DELTA("text_delta"),

			@JsonProperty("thinking_delta")
			THINKING_DELTA("thinking_delta"),

			@JsonProperty("signature_delta")
			SIGNATURE_DELTA("signature_delta"),

			@JsonProperty("input_json_delta")
			INPUT_JSON_DELTA("input_json_delta"),

			@JsonProperty("image")
			IMAGE("image"),

			@JsonProperty("document")
			DOCUMENT("document"),

			@JsonProperty("thinking")
			THINKING("thinking"),

			@JsonProperty("redacted_thinking")
			REDACTED_THINKING("redacted_thinking");

			public final String value;

			Type(String value) {
				this.value = value;
			}

			public String getValue() {
				return this.value;
			}

		}

		@JsonInclude(Include.NON_NULL)
		public record Source(
		// @formatter:off
			@JsonProperty("type") String type,
			@JsonProperty("media_type") String mediaType,
			@JsonProperty("data") String data) {
			// @formatter:on

			public Source(String mediaType, String data) {
				this("base64", mediaType, data);
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record Tool(
	// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("description") String description,
		@JsonProperty("input_schema") Map<String, Object> inputSchema) {
		// @formatter:on
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionResponse(
	// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("type") String type,
		@JsonProperty("role") Role role,
		@JsonProperty("content") List<ContentBlock> content,
		@JsonProperty("model") String model,
		@JsonProperty("stop_reason") String stopReason,
		@JsonProperty("stop_sequence") String stopSequence,
		@JsonProperty("usage") Usage usage) {
		// @formatter:on
	}

	@JsonInclude(Include.NON_NULL)
	public record Usage(
	// @formatter:off
		@JsonProperty("input_tokens") Integer inputTokens,
		@JsonProperty("output_tokens") Integer outputTokens) {
		// @formatter:off
	}

	public static class ToolUseAggregationEvent implements StreamEvent {

		private Integer index;

		private String id;

		private String name;

		private String partialJson = "";

		private List<ContentBlockStartEvent.ContentBlockToolUse> toolContentBlocks = new ArrayList<>();

		@Override
		public EventType type() {
			return EventType.TOOL_USE_AGGREGATE;
		}

		public List<ContentBlockStartEvent.ContentBlockToolUse> getToolContentBlocks() {
			return this.toolContentBlocks;
		}

		public boolean isEmpty() {
			return (this.index == null || this.id == null || this.name == null
					|| !StringUtils.hasText(this.partialJson));
		}

		ToolUseAggregationEvent withIndex(Integer index) {
			this.index = index;
			return this;
		}

		ToolUseAggregationEvent withId(String id) {
			this.id = id;
			return this;
		}

		ToolUseAggregationEvent withName(String name) {
			this.name = name;
			return this;
		}

		ToolUseAggregationEvent appendPartialJson(String partialJson) {
			this.partialJson = this.partialJson + partialJson;
			return this;
		}

		void squashIntoContentBlock() {
			Map<String, Object> map = (StringUtils.hasText(this.partialJson))
					? ModelOptionsUtils.jsonToMap(this.partialJson) : Map.of();
			this.toolContentBlocks.add(new ContentBlockStartEvent.ContentBlockToolUse("tool_use", this.id, this.name, map));
			this.index = null;
			this.id = null;
			this.name = null;
			this.partialJson = "";
		}

		@Override
		public String toString() {
			return "EventToolUseBuilder [index=" + this.index + ", id=" + this.id + ", name=" + this.name + ", partialJson="
					+ this.partialJson + ", toolUseMap=" + this.toolContentBlocks + "]";
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ContentBlockStartEvent(
			// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("index") Integer index,
		@JsonProperty("content_block") ContentBlockBody contentBlock) implements StreamEvent {

		@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
				visible = true)
		@JsonSubTypes({ @JsonSubTypes.Type(value = ContentBlockToolUse.class, name = "tool_use"),
				@JsonSubTypes.Type(value = ContentBlockText.class, name = "text") })
		public interface ContentBlockBody {
			String type();
		}

		@JsonInclude(Include.NON_NULL)
		public record ContentBlockToolUse(
			@JsonProperty("type") String type,
			@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("input") Map<String, Object> input) implements ContentBlockBody {
		}

		@JsonInclude(Include.NON_NULL)
		public record ContentBlockText(
			@JsonProperty("type") String type,
			@JsonProperty("text") String text) implements ContentBlockBody {
		}
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record ContentBlockDeltaEvent(
	// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("index") Integer index,
		@JsonProperty("delta") ContentBlockDeltaBody delta) implements StreamEvent {

		@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type",
				visible = true)
		@JsonSubTypes({ @JsonSubTypes.Type(value = ContentBlockDeltaText.class, name = "text_delta"),
				@JsonSubTypes.Type(value = ContentBlockDeltaJson.class, name = "input_json_delta"),
				@JsonSubTypes.Type(value = ContentBlockDeltaThinking.class, name = "thinking_delta"),
				@JsonSubTypes.Type(value = ContentBlockDeltaSignature.class, name = "signature_delta")
		})
		public interface ContentBlockDeltaBody {
			String type();
		}

		@JsonInclude(Include.NON_NULL)
		public record ContentBlockDeltaText(
			@JsonProperty("type") String type,
			@JsonProperty("text") String text) implements ContentBlockDeltaBody {
		}

		@JsonInclude(Include.NON_NULL)
		public record ContentBlockDeltaJson(
			@JsonProperty("type") String type,
			@JsonProperty("partial_json") String partialJson) implements ContentBlockDeltaBody {
		}

		public record ContentBlockDeltaThinking(
			@JsonProperty("type") String type,
			@JsonProperty("thinking") String thinking) implements ContentBlockDeltaBody {
		}

		public record ContentBlockDeltaSignature(
			@JsonProperty("type") String type,
			@JsonProperty("signature") String signature) implements ContentBlockDeltaBody {
		}
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record ContentBlockStopEvent(
	// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("index") Integer index) implements StreamEvent {
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record MessageStartEvent(// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("message") ChatCompletionResponse message) implements StreamEvent {
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record MessageDeltaEvent(
	// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("delta") MessageDelta delta,
		@JsonProperty("usage") MessageDeltaUsage usage) implements StreamEvent {

		@JsonInclude(Include.NON_NULL)
		public record MessageDelta(
			@JsonProperty("stop_reason") String stopReason,
			@JsonProperty("stop_sequence") String stopSequence) {
		}

		@JsonInclude(Include.NON_NULL)
		public record MessageDeltaUsage(
			@JsonProperty("output_tokens") Integer outputTokens) {
		}
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record MessageStopEvent(

			@JsonProperty("type") EventType type) implements StreamEvent {
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record ErrorEvent(
	// @formatter:off
		@JsonProperty("type") EventType type,
		@JsonProperty("error") Error error) implements StreamEvent {

		@JsonInclude(Include.NON_NULL)
		public record Error(
			@JsonProperty("type") String type,
			@JsonProperty("message") String message) {
		}
	}
	// @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record PingEvent(
	// @formatter:off
		@JsonProperty("type") EventType type) implements StreamEvent {
	}
	// @formatter:on

}
