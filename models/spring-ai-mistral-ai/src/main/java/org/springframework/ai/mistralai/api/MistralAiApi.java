package org.springframework.ai.mistralai.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.observation.conventions.AiProvider;
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

public class MistralAiApi {

	public static final String PROVIDER_NAME = AiProvider.MISTRAL_AI.value();

	private static final String DEFAULT_BASE_URL = "https://api.mistral.ai";

	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final RestClient restClient;

	private final WebClient webClient;

	private final MistralAiStreamFunctionCallingHelper chunkMerger = new MistralAiStreamFunctionCallingHelper();

	public MistralAiApi(String mistralAiApiKey) {
		this(DEFAULT_BASE_URL, mistralAiApiKey);
	}

	public MistralAiApi(String baseUrl, String mistralAiApiKey) {
		this(baseUrl, mistralAiApiKey, RestClient.builder(), RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public MistralAiApi(String baseUrl, String mistralAiApiKey, RestClient.Builder restClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.setBearerAuth(mistralAiApiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(jsonContentHeaders)
			.defaultStatusHandler(responseErrorHandler)
			.build();

		this.webClient = WebClient.builder().baseUrl(baseUrl).defaultHeaders(jsonContentHeaders).build();
	}

	public <T> ResponseEntity<EmbeddingList<Embedding>> embeddings(EmbeddingRequest<T> embeddingRequest) {

		Assert.notNull(embeddingRequest, "The request body can not be null.");

		Assert.notNull(embeddingRequest.input(), "The input can not be null.");
		Assert.isTrue(embeddingRequest.input() instanceof String || embeddingRequest.input() instanceof List,
				"The input must be either a String, or a List of Strings or List of List of integers.");

		if (embeddingRequest.input() instanceof List list) {
			Assert.isTrue(!CollectionUtils.isEmpty(list), "The input list can not be empty.");
			Assert.isTrue(list.size() <= 1024, "The list must be 1024 dimensions or less");
			Assert.isTrue(
					list.get(0) instanceof String || list.get(0) instanceof Integer || list.get(0) instanceof List,
					"The input must be either a String, or a List of Strings or list of list of integers.");
		}

		return this.restClient.post()
			.uri("/v1/embeddings")
			.body(embeddingRequest)
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {

			});
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
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

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
				Mono<ChatCompletionChunk> mono1 = window.reduce(
						new ChatCompletionChunk(null, null, null, null, null, null),
						(previous, current) -> this.chunkMerger.merge(previous, current));
				return List.of(mono1);
			})
			.flatMap(mono -> mono);
	}

	public enum ChatCompletionFinishReason {

		// @formatter:off

		@JsonProperty("stop")
		STOP,

		@JsonProperty("length")
		LENGTH,

		@JsonProperty("model_length")
		MODEL_LENGTH,

		@JsonProperty("error")
		ERROR,

		@JsonProperty("tool_calls")
		TOOL_CALLS
		 // @formatter:on

	}

	public enum ChatModel implements ChatModelDescription {

		// @formatter:off

		CODESTRAL("codestral-latest"),
		LARGE("mistral-large-latest"),
		PIXTRAL_LARGE("pixtral-large-latest"),
		MINISTRAL_3B_LATEST("ministral-3b-latest"),
		MINISTRAL_8B_LATEST("ministral-8b-latest"),

		SMALL("mistral-small-latest"),
		PIXTRAL("pixtral-12b-2409"),

		OPEN_MISTRAL_NEMO("open-mistral-nemo"),
		OPEN_CODESTRAL_MAMBA("open-codestral-mamba");
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

	public enum EmbeddingModel {

		// @formatter:off
		EMBED("mistral-embed");
		 // @formatter:on

		private final String value;

		EmbeddingModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}

	@JsonInclude(Include.NON_NULL)
	public static class FunctionTool {

		@JsonProperty("type")
		Type type = Type.FUNCTION;

		@JsonProperty("function")
		Function function;

		public FunctionTool() {

		}

		public FunctionTool(Function function) {
			this(Type.FUNCTION, function);
		}

		public FunctionTool(Type type, Function function) {
			this.type = type;
			this.function = function;
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

			public Function(String description, String name, Map<String, Object> parameters) {
				this.description = description;
				this.name = name;
				this.parameters = parameters;
			}

			public Function(String description, String name, String jsonSchema) {
				this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema));
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
	public record Usage(
	// @formatter:off
		@JsonProperty("prompt_tokens") Integer promptTokens,
		@JsonProperty("total_tokens") Integer totalTokens,
		@JsonProperty("completion_tokens") Integer completionTokens) {
		 // @formatter:on
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

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Embedding embedding1)) {
				return false;
			}
			return Objects.equals(this.index, embedding1.index) && Arrays.equals(this.embedding, embedding1.embedding)
					&& Objects.equals(this.object, embedding1.object);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(this.index, this.object);
			result = 31 * result + Arrays.hashCode(this.embedding);
			return result;
		}

		@Override
		public String toString() {
			return "Embedding{" + "index=" + this.index + ", embedding=" + Arrays.toString(this.embedding)
					+ ", object='" + this.object + '\'' + '}';
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest<T>(
	// @formatter:off
		@JsonProperty("input") T input,
		@JsonProperty("model") String model,
		@JsonProperty("encoding_format") String encodingFormat) {
		 // @formatter:on

		public EmbeddingRequest(T input, String model) {
			this(input, model, "float");
		}

		public EmbeddingRequest(T input) {
			this(input, EmbeddingModel.EMBED.getValue());
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingList<T>(
	// @formatter:off
			@JsonProperty("object") String object,
			@JsonProperty("data") List<T> data,
			@JsonProperty("model") String model,
			@JsonProperty("usage") Usage usage) {
		 // @formatter:on
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionRequest(
	// @formatter:off
			@JsonProperty("model") String model,
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("tool_choice") ToolChoice toolChoice,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP,
			@JsonProperty("max_tokens") Integer maxTokens,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("safe_prompt") Boolean safePrompt,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("random_seed") Integer randomSeed,
			@JsonProperty("response_format") ResponseFormat responseFormat) {
		 // @formatter:on

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model) {
			this(model, messages, null, null, 0.7, 1.0, null, false, false, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature,
				boolean stream) {
			this(model, messages, null, null, temperature, 1.0, null, stream, false, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
			this(model, messages, null, null, temperature, 1.0, null, false, false, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, List<FunctionTool> tools,
				ToolChoice toolChoice) {
			this(model, messages, tools, toolChoice, null, 1.0, null, false, false, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
			this(null, messages, null, null, 0.7, 1.0, null, stream, false, null, null, null);
		}

		public enum ToolChoice {

			// @formatter:off
			@JsonProperty("auto")
			AUTO,
			@JsonProperty("any")
			ANY,
			@JsonProperty("none")
			NONE
			 // @formatter:on

		}

		@JsonInclude(Include.NON_NULL)
		public record ResponseFormat(@JsonProperty("type") String type,
				@JsonProperty("json_schema") Map<String, Object> jsonSchema) {
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionMessage(
	// @formatter:off
		@JsonProperty("content") Object rawContent,
		@JsonProperty("role") Role role,
		@JsonProperty("name") String name,
		@JsonProperty("tool_calls") List<ToolCall> toolCalls,
		@JsonProperty("tool_call_id") String toolCallId) {
		// @formatter:on

		public ChatCompletionMessage(Object content, Role role, String name, List<ToolCall> toolCalls) {
			this(content, role, name, toolCalls, null);
		}

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

			// @formatter:off
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
				@JsonProperty("function") ChatCompletionFunction function, @JsonProperty("index") Integer index) {

		}

		@JsonInclude(Include.NON_NULL)
		public record ChatCompletionFunction(@JsonProperty("name") String name,
				@JsonProperty("arguments") String arguments) {

		}

		@JsonInclude(Include.NON_NULL)
		public record MediaContent(
		// @formatter:off
		   		@JsonProperty("type") String type,
		   		@JsonProperty("text") String text,
		   		@JsonProperty("image_url") ImageUrl imageUrl
				// @formatter:on
		) {

			public MediaContent(String text) {
				this("text", text, null);
			}

			public MediaContent(ImageUrl imageUrl) {
				this("image_url", null, imageUrl);
			}

			@JsonInclude(Include.NON_NULL)
			public record ImageUrl(
			// @formatter:off
					@JsonProperty("url") String url,
					@JsonProperty("detail") String detail
					// @formatter:on
			) {

				public ImageUrl(String url) {
					this(url, null);
				}

			}

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
			@JsonProperty("logprobs") LogProbs logprobs) {
			 // @formatter:on
		}

	}

	@JsonInclude(Include.NON_NULL)
	public record LogProbs(@JsonProperty("content") List<Content> content) {

		@JsonInclude(Include.NON_NULL)
		public record Content(@JsonProperty("token") String token, @JsonProperty("logprob") Float logprob,
				@JsonProperty("bytes") List<Integer> probBytes,
				@JsonProperty("top_logprobs") List<TopLogProbs> topLogprobs) {

			@JsonInclude(Include.NON_NULL)
			public record TopLogProbs(@JsonProperty("token") String token, @JsonProperty("logprob") Float logprob,
					@JsonProperty("bytes") List<Integer> probBytes) {

			}

		}

	}

	@JsonInclude(Include.NON_NULL)
	public record ChatCompletionChunk(
	// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("object") String object,
		@JsonProperty("created") Long created,
		@JsonProperty("model") String model,
		@JsonProperty("choices") List<ChunkChoice> choices,
		@JsonProperty("usage") Usage usage) {
		 // @formatter:on

		@JsonInclude(Include.NON_NULL)
		public record ChunkChoice(
		// @formatter:off
			@JsonProperty("index") Integer index,
			@JsonProperty("delta") ChatCompletionMessage delta,
			@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
		@JsonProperty("logprobs") LogProbs logprobs) {
			 // @formatter:on
		}

	}

}
