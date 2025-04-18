package org.springframework.ai.openai.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class OpenAiApi {

	public static Builder builder() {
		return new Builder();
	}

	public static final OpenAiApi.ChatModel DEFAULT_CHAT_MODEL = ChatModel.GPT_4_O;

	public static final String DEFAULT_EMBEDDING_MODEL = EmbeddingModel.TEXT_EMBEDDING_ADA_002.getValue();

	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final String completionsPath;

	private final String embeddingsPath;

	private final RestClient restClient;

	private final WebClient webClient;

	private OpenAiStreamFunctionCallingHelper chunkMerger = new OpenAiStreamFunctionCallingHelper();

	public OpenAiApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers, String completionsPath,
			String embeddingsPath, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		Assert.hasText(completionsPath, "Completions Path must not be null");
		Assert.hasText(embeddingsPath, "Embeddings Path must not be null");
		Assert.notNull(headers, "Headers must not be null");

		this.completionsPath = completionsPath;
		this.embeddingsPath = embeddingsPath;
		// @formatter:off
		Consumer<HttpHeaders> finalHeaders = h -> {
			if (!(apiKey instanceof NoopApiKey)) {
				h.setBearerAuth(apiKey.getValue());
			}

			h.setContentType(MediaType.APPLICATION_JSON);
			h.addAll(headers);
		};
		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(finalHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();

		this.webClient = webClientBuilder
			.baseUrl(baseUrl)
			.defaultHeaders(finalHeaders)
			.build(); // @formatter:on
	}

	public static String getTextContent(List<ChatCompletionMessage.MediaContent> content) {
		return content.stream()
			.filter(c -> "text".equals(c.type()))
			.map(ChatCompletionMessage.MediaContent::text)
			.reduce("", (a, b) -> a + b);
	}

	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest) {
		return chatCompletionEntity(chatRequest, new LinkedMultiValueMap<>());
	}

	public ResponseEntity<ChatCompletion> chatCompletionEntity(ChatCompletionRequest chatRequest,
			MultiValueMap<String, String> additionalHttpHeader) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(!chatRequest.stream(), "Request must set the stream property to false.");
		Assert.notNull(additionalHttpHeader, "The additional HTTP headers can not be null.");

		return this.restClient.post()
			.uri(this.completionsPath)
			.headers(headers -> headers.addAll(additionalHttpHeader))
			.body(chatRequest)
			.retrieve()
			.toEntity(ChatCompletion.class);
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {
		return chatCompletionStream(chatRequest, new LinkedMultiValueMap<>());
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest,
			MultiValueMap<String, String> additionalHttpHeader) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		return this.webClient.post()
			.uri(this.completionsPath)
			.headers(headers -> headers.addAll(additionalHttpHeader))
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
						new ChatCompletionChunk(null, null, null, null, null, null, null, null),
						(previous, current) -> this.chunkMerger.merge(previous, current));
				return List.of(monoChunk);
			})

			.flatMap(mono -> mono);
	}

	public <T> ResponseEntity<EmbeddingList<Embedding>> embeddings(EmbeddingRequest<T> embeddingRequest) {

		Assert.notNull(embeddingRequest, "The request body can not be null.");

		Assert.notNull(embeddingRequest.input(), "The input can not be null.");
		Assert.isTrue(embeddingRequest.input() instanceof String || embeddingRequest.input() instanceof List,
				"The input must be either a String, or a List of Strings or List of List of integers.");

		if (embeddingRequest.input() instanceof List list) {
			Assert.isTrue(!CollectionUtils.isEmpty(list), "The input list can not be empty.");
			Assert.isTrue(list.size() <= 2048, "The list must be 2048 dimensions or less");
			Assert.isTrue(
					list.get(0) instanceof String || list.get(0) instanceof Integer || list.get(0) instanceof List,
					"The input must be either a String, or a List of Strings or list of list of integers.");
		}

		return this.restClient.post()
			.uri(this.embeddingsPath)
			.body(embeddingRequest)
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {

			});
	}

	public enum ChatModel implements ChatModelDescription {

		O4_MINI("o4-mini"),

		O3("o3"),

		O3_MINI("o3-mini"),

		O1("o1"),

		O1_MINI("o1-mini"),

		O1_PRO("o1-pro"),

		GPT_4_1("gpt-4.1"),

		GPT_4_O("gpt-4o"),

		CHATGPT_4_O_LATEST("chatgpt-4o-latest"),

		GPT_4_O_AUDIO_PREVIEW("gpt-4o-audio-preview"),

		GPT_4_1_MINI("gpt-4.1-mini"),

		GPT_4_1_NANO("gpt-4.1-nano"),

		GPT_4_O_MINI("gpt-4o-mini"),

		GPT_4_O_MINI_AUDIO_PREVIEW("gpt-4o-mini-audio-preview"),

		GPT_4O_REALTIME_PREVIEW("gpt-4o-realtime-preview"),

		GPT_4O_MINI_REALTIME_PREVIEW("gpt-4o-mini-realtime-preview\n"),

		GPT_4_TURBO("gpt-4-turbo"),

		GPT_4("gpt-4"),

		GPT_3_5_TURBO("gpt-3.5-turbo"),

		GPT_3_5_TURBO_INSTRUCT("gpt-3.5-turbo-instruct");

		public final String value;

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

		TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),

		TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),

		TEXT_EMBEDDING_ADA_002("text-embedding-ada-002");

		public final String value;

		EmbeddingModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class FunctionTool {

		@JsonProperty("type")
		private Type type = Type.FUNCTION;

		@JsonProperty("function")
		private Function function;

		public FunctionTool() {

		}

		public FunctionTool(Type type, Function function) {
			this.type = type;
			this.function = function;
		}

		public FunctionTool(Function function) {
			this(Type.FUNCTION, function);
		}

		public Type getType() {
			return this.type;
		}

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
			FUNCTION

		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Function {

			@JsonProperty("description")
			private String description;

			@JsonProperty("name")
			private String name;

			@JsonProperty("parameters")
			private Map<String, Object> parameters;

			@JsonProperty("strict")
			Boolean strict;

			@JsonIgnore
			private String jsonSchema;

			@SuppressWarnings("unused")
			private Function() {
			}

			public Function(String description, String name, Map<String, Object> parameters, Boolean strict) {
				this.description = description;
				this.name = name;
				this.parameters = parameters;
				this.strict = strict;
			}

			public Function(String description, String name, String jsonSchema) {
				this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema), null);
			}

			public String getDescription() {
				return this.description;
			}

			public String getName() {
				return this.name;
			}

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

			public Boolean getStrict() {
				return this.strict;
			}

			public void setStrict(Boolean strict) {
				this.strict = strict;
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

	public enum OutputModality {

		// @formatter:off
		@JsonProperty("audio")
		AUDIO,
		@JsonProperty("text")
		TEXT
		// @formatter:on

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(// @formatter:off
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("model") String model,
			@JsonProperty("store") Boolean store,
			@JsonProperty("metadata") Map<String, String> metadata,
			@JsonProperty("frequency_penalty") Double frequencyPenalty,
			@JsonProperty("logit_bias") Map<String, Integer> logitBias,
			@JsonProperty("logprobs") Boolean logprobs,
			@JsonProperty("top_logprobs") Integer topLogprobs,
			@JsonProperty("max_tokens") @Deprecated Integer maxTokens,
			@JsonProperty("max_completion_tokens") Integer maxCompletionTokens,
			@JsonProperty("n") Integer n,
			@JsonProperty("modalities") List<OutputModality> outputModalities,
			@JsonProperty("audio") AudioParameters audioParameters,
			@JsonProperty("presence_penalty") Double presencePenalty,
			@JsonProperty("response_format") ResponseFormat responseFormat,
			@JsonProperty("seed") Integer seed,
			@JsonProperty("service_tier") String serviceTier,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("stream_options") StreamOptions streamOptions,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("tool_choice") Object toolChoice,
			@JsonProperty("parallel_tool_calls") Boolean parallelToolCalls,
			@JsonProperty("user") String user,
			@JsonProperty("reasoning_effort") String reasoningEffort) {

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
			this(messages, model, null, null, null, null, null, null, null, null, null, null, null, null, null,
					null, null, null, false, null, temperature, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, AudioParameters audio, boolean stream) {
			this(messages, model, null, null, null, null, null, null,
					null, null, null, List.of(OutputModality.AUDIO, OutputModality.TEXT), audio, null, null,
					null, null, null, stream, null, null, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature, boolean stream) {
			this(messages, model, null, null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, stream, null, temperature, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model,
				List<FunctionTool> tools, Object toolChoice) {
			this(messages, model, null, null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, false, null, 0.8, null,
					tools, toolChoice, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
			this(messages, null, null, null, null, null, null, null, null, null, null,
					null, null, null, null, null, null, null, stream, null, null, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest streamOptions(StreamOptions streamOptions) {
			return new ChatCompletionRequest(this.messages, this.model, this.store, this.metadata, this.frequencyPenalty, this.logitBias, this.logprobs,
			this.topLogprobs, this.maxTokens, this.maxCompletionTokens, this.n, this.outputModalities, this.audioParameters, this.presencePenalty,
			this.responseFormat, this.seed, this.serviceTier, this.stop, this.stream, streamOptions, this.temperature, this.topP,
			this.tools, this.toolChoice, this.parallelToolCalls, this.user, this.reasoningEffort);
		}

		public static class ToolChoiceBuilder {

			public static final String AUTO = "auto";

			public static final String NONE = "none";

			public static Object FUNCTION(String functionName) {
				return Map.of("type", "function", "function", Map.of("name", functionName));
			}
		}

		@JsonInclude(Include.NON_NULL)
		public record AudioParameters(
				@JsonProperty("voice") Voice voice,
				@JsonProperty("format") AudioResponseFormat format) {

			public enum Voice {

				@JsonProperty("alloy") ALLOY,

				@JsonProperty("echo") ECHO,

				@JsonProperty("fable") FABLE,

				@JsonProperty("onyx") ONYX,

				@JsonProperty("nova") NOVA,

				@JsonProperty("shimmer") SHIMMER
			}

			public enum AudioResponseFormat {

				@JsonProperty("mp3") MP3,

				@JsonProperty("flac") FLAC,

				@JsonProperty("opus") OPUS,

				@JsonProperty("pcm16") PCM16,

				@JsonProperty("wav") WAV
			}
		}

		@JsonInclude(Include.NON_NULL)
		public record StreamOptions(
				@JsonProperty("include_usage") Boolean includeUsage) {

			public static StreamOptions INCLUDE_USAGE = new StreamOptions(true);
		}
	} // @formatter:on

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(// @formatter:off
			@JsonProperty("content") Object rawContent,
			@JsonProperty("role") Role role,
			@JsonProperty("name") String name,
			@JsonProperty("tool_call_id") String toolCallId,
			@JsonProperty("tool_calls")
			@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<ToolCall> toolCalls,
			@JsonProperty("refusal") String refusal,
			@JsonProperty("audio") AudioOutput audioOutput) { // @formatter:on

		public ChatCompletionMessage(Object content, Role role) {
			this(content, role, null, null, null, null, null);

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
		public record MediaContent(// @formatter:off
			@JsonProperty("type") String type,
			@JsonProperty("text") String text,
			@JsonProperty("image_url") ImageUrl imageUrl,
			@JsonProperty("input_audio") InputAudio inputAudio) { // @formatter:on

			public MediaContent(String text) {
				this("text", text, null, null);
			}

			public MediaContent(ImageUrl imageUrl) {
				this("image_url", null, imageUrl, null);
			}

			public MediaContent(InputAudio inputAudio) {
				this("input_audio", null, null, inputAudio);
			}

			@JsonInclude(Include.NON_NULL)
			public record InputAudio(// @formatter:off
				@JsonProperty("data") String data,
				@JsonProperty("format") Format format) {

				public enum Format {

					@JsonProperty("mp3") MP3,

					@JsonProperty("wav") WAV
				} // @formatter:on
			}

			@JsonInclude(Include.NON_NULL)
			public record ImageUrl(@JsonProperty("url") String url, @JsonProperty("detail") String detail) {

				public ImageUrl(String url) {
					this(url, null);
				}

			}

		}

		@JsonInclude(Include.NON_NULL)
		public record ToolCall(// @formatter:off
				@JsonProperty("index") Integer index,
				@JsonProperty("id") String id,
				@JsonProperty("type") String type,
				@JsonProperty("function") ChatCompletionFunction function) { // @formatter:on

			public ToolCall(String id, String type, ChatCompletionFunction function) {
				this(null, id, type, function);
			}

		}

		@JsonInclude(Include.NON_NULL)
		public record ChatCompletionFunction(// @formatter:off
				@JsonProperty("name") String name,
				@JsonProperty("arguments") String arguments) { // @formatter:on
		}

		@JsonInclude(Include.NON_NULL)
		public record AudioOutput(// @formatter:off
				@JsonProperty("id") String id,
				@JsonProperty("data") String data,
				@JsonProperty("expires_at") Long expiresAt,
				@JsonProperty("transcript") String transcript
		) { // @formatter:on
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletion(// @formatter:off
			@JsonProperty("id") String id,
			@JsonProperty("choices") List<Choice> choices,
			@JsonProperty("created") Long created,
			@JsonProperty("model") String model,
			@JsonProperty("service_tier") String serviceTier,
			@JsonProperty("system_fingerprint") String systemFingerprint,
			@JsonProperty("object") String object,
			@JsonProperty("usage") Usage usage
	) { // @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record Choice(// @formatter:off
				@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
				@JsonProperty("index") Integer index,
				@JsonProperty("message") ChatCompletionMessage message,
				@JsonProperty("logprobs") LogProbs logprobs) { // @formatter:on
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record LogProbs(@JsonProperty("content") List<Content> content,
			@JsonProperty("refusal") List<Content> refusal) {

		@JsonInclude(Include.NON_NULL)
		public record Content(// @formatter:off
				@JsonProperty("token") String token,
				@JsonProperty("logprob") Float logprob,
				@JsonProperty("bytes") List<Integer> probBytes,
				@JsonProperty("top_logprobs") List<TopLogProbs> topLogprobs) { // @formatter:on

			@JsonInclude(Include.NON_NULL)
			public record TopLogProbs(// @formatter:off
					@JsonProperty("token") String token,
					@JsonProperty("logprob") Float logprob,
					@JsonProperty("bytes") List<Integer> probBytes) { // @formatter:on
			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Usage(// @formatter:off
		@JsonProperty("completion_tokens") Integer completionTokens,
		@JsonProperty("prompt_tokens") Integer promptTokens,
		@JsonProperty("total_tokens") Integer totalTokens,
		@JsonProperty("prompt_tokens_details") PromptTokensDetails promptTokensDetails,
		@JsonProperty("completion_tokens_details") CompletionTokenDetails completionTokenDetails,
		@JsonProperty("prompt_cache_hit_tokens") Integer promptCacheHitTokens,
		@JsonProperty("prompt_cache_miss_tokens") Integer promptCacheMissTokens) { // @formatter:on

		public Usage(Integer completionTokens, Integer promptTokens, Integer totalTokens) {
			this(completionTokens, promptTokens, totalTokens, null, null, null, null);
		}

		@JsonInclude(Include.NON_NULL)
		public record PromptTokensDetails(// @formatter:off
			@JsonProperty("audio_tokens") Integer audioTokens,
			@JsonProperty("cached_tokens") Integer cachedTokens) { // @formatter:on
		}

		@JsonInclude(Include.NON_NULL)
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record CompletionTokenDetails(// @formatter:off
			@JsonProperty("reasoning_tokens") Integer reasoningTokens,
			@JsonProperty("accepted_prediction_tokens") Integer acceptedPredictionTokens,
			@JsonProperty("audio_tokens") Integer audioTokens,
			@JsonProperty("rejected_prediction_tokens") Integer rejectedPredictionTokens) { // @formatter:on
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionChunk(// @formatter:off
			@JsonProperty("id") String id,
			@JsonProperty("choices") List<ChunkChoice> choices,
			@JsonProperty("created") Long created,
			@JsonProperty("model") String model,
			@JsonProperty("service_tier") String serviceTier,
			@JsonProperty("system_fingerprint") String systemFingerprint,
			@JsonProperty("object") String object,
			@JsonProperty("usage") Usage usage) { // @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record ChunkChoice(// @formatter:off
				@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
				@JsonProperty("index") Integer index,
				@JsonProperty("delta") ChatCompletionMessage delta,
				@JsonProperty("logprobs") LogProbs logprobs) { // @formatter:on

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record Embedding(// @formatter:off
			@JsonProperty("index") Integer index,
			@JsonProperty("embedding") float[] embedding,
			@JsonProperty("object") String object) { // @formatter:on

		public Embedding(Integer index, float[] embedding) {
			this(index, embedding, "embedding");
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest<T>(// @formatter:off
			@JsonProperty("input") T input,
			@JsonProperty("model") String model,
			@JsonProperty("encoding_format") String encodingFormat,
			@JsonProperty("dimensions") Integer dimensions,
			@JsonProperty("user") String user) { // @formatter:on

		public EmbeddingRequest(T input, String model) {
			this(input, model, "float", null, null);
		}

		public EmbeddingRequest(T input) {
			this(input, DEFAULT_EMBEDDING_MODEL);
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingList<T>(// @formatter:off
			@JsonProperty("object") String object,
			@JsonProperty("data") List<T> data,
			@JsonProperty("model") String model,
			@JsonProperty("usage") Usage usage) { // @formatter:on
	}

	public static class Builder {

		private String baseUrl = OpenAiApiConstants.DEFAULT_BASE_URL;

		private ApiKey apiKey;

		private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

		private String completionsPath = "/v1/chat/completions";

		private String embeddingsPath = "/v1/embeddings";

		private RestClient.Builder restClientBuilder = RestClient.builder();

		private WebClient.Builder webClientBuilder = WebClient.builder();

		private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

		public Builder baseUrl(String baseUrl) {
			Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
			this.baseUrl = baseUrl;
			return this;
		}

		public Builder apiKey(ApiKey apiKey) {
			Assert.notNull(apiKey, "apiKey cannot be null");
			this.apiKey = apiKey;
			return this;
		}

		public Builder apiKey(String simpleApiKey) {
			Assert.notNull(simpleApiKey, "simpleApiKey cannot be null");
			this.apiKey = new SimpleApiKey(simpleApiKey);
			return this;
		}

		public Builder headers(MultiValueMap<String, String> headers) {
			Assert.notNull(headers, "headers cannot be null");
			this.headers = headers;
			return this;
		}

		public Builder completionsPath(String completionsPath) {
			Assert.hasText(completionsPath, "completionsPath cannot be null or empty");
			this.completionsPath = completionsPath;
			return this;
		}

		public Builder embeddingsPath(String embeddingsPath) {
			Assert.hasText(embeddingsPath, "embeddingsPath cannot be null or empty");
			this.embeddingsPath = embeddingsPath;
			return this;
		}

		public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
			Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
			this.restClientBuilder = restClientBuilder;
			return this;
		}

		public Builder webClientBuilder(WebClient.Builder webClientBuilder) {
			Assert.notNull(webClientBuilder, "webClientBuilder cannot be null");
			this.webClientBuilder = webClientBuilder;
			return this;
		}

		public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
			Assert.notNull(responseErrorHandler, "responseErrorHandler cannot be null");
			this.responseErrorHandler = responseErrorHandler;
			return this;
		}

		public OpenAiApi build() {
			Assert.notNull(this.apiKey, "apiKey must be set");
			return new OpenAiApi(this.baseUrl, this.apiKey, this.headers, this.completionsPath, this.embeddingsPath,
					this.restClientBuilder, this.webClientBuilder, this.responseErrorHandler);
		}

	}

}
