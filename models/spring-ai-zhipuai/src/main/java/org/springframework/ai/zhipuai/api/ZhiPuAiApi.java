package org.springframework.ai.zhipuai.api;

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

public class ZhiPuAiApi {

	public static final String DEFAULT_CHAT_MODEL = ChatModel.GLM_4_Air.getValue();
	public static final String DEFAULT_EMBEDDING_MODEL = EmbeddingModel.Embedding_2.getValue();
	private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

	private final RestClient restClient;

	private final WebClient webClient;

	private final ZhiPuAiStreamFunctionCallingHelper chunkMerger = new ZhiPuAiStreamFunctionCallingHelper();

	public ZhiPuAiApi(String zhiPuAiToken) {
		this(ZhiPuApiConstants.DEFAULT_BASE_URL, zhiPuAiToken);
	}

	public ZhiPuAiApi(String baseUrl, String zhiPuAiToken) {
		this(baseUrl, zhiPuAiToken, RestClient.builder());
	}

	public ZhiPuAiApi(String baseUrl, String zhiPuAiToken, RestClient.Builder restClientBuilder) {
		this(baseUrl, zhiPuAiToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	public ZhiPuAiApi(String baseUrl, String zhiPuAiToken, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> authHeaders = h -> {
			h.setBearerAuth(zhiPuAiToken);
			h.setContentType(MediaType.APPLICATION_JSON);
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
				.uri("/v4/chat/completions")
				.body(chatRequest)
				.retrieve()
				.toEntity(ChatCompletion.class);
	}

	public Flux<ChatCompletionChunk> chatCompletionStream(ChatCompletionRequest chatRequest) {

		Assert.notNull(chatRequest, "The request body can not be null.");
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		return this.webClient.post()
				.uri("/v4/chat/completions")
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
							this.chunkMerger::merge);
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
			Assert.isTrue(list.size() <= 512, "The list must be 512 dimensions or less");
			Assert.isTrue(list.get(0) instanceof String || list.get(0) instanceof Integer
					|| list.get(0) instanceof List,
					"The input must be either a String, or a List of Strings or list of list of integers.");
		}

		return this.restClient.post()
				.uri("/v4/embeddings")
				.body(embeddingRequest)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>() {
		});
	}

	public enum ChatModel implements ChatModelDescription {
		GLM_4("GLM-4"),
		GLM_4V("glm-4v"),
		GLM_4_Air("glm-4-air"),
		GLM_4_AirX("glm-4-airx"),
		GLM_4_Flash("glm-4-flash"),
		GLM_3_Turbo("GLM-3-Turbo");

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

		Embedding_2("Embedding-2"),

		Embedding_3("Embedding-3");

		public final String  value;

		EmbeddingModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public class Foo {

		String foo;

		public Foo() {

		}
		public Foo(String foo) {
			this.foo = foo;
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

		public FunctionTool(
				Type type,
				Function function) {
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
	public record ChatCompletionRequest(
			@JsonProperty("messages") List<ChatCompletionMessage> messages,
			@JsonProperty("model") String model,
			@JsonProperty("max_tokens") Integer maxTokens,
			@JsonProperty("stop") List<String> stop,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("temperature") Double temperature,
			@JsonProperty("top_p") Double topP,
			@JsonProperty("tools") List<FunctionTool> tools,
			@JsonProperty("tool_choice") Object toolChoice,
			@JsonProperty("user") String user,
			@JsonProperty("request_id") String requestId,
			@JsonProperty("do_sample") Boolean doSample) {

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature) {
			this(messages, model, null,  null, false, temperature, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Double temperature, boolean stream) {
			this(messages, model, null,  null,  stream, temperature, null,
					null, null, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model,
				List<FunctionTool> tools, Object toolChoice) {
			this(messages, model, null, null,  false, 0.8, null,
					tools, toolChoice, null, null, null);
		}

		public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
			this(messages, null, null,  null,  stream, null, null,
					null, null, null, null, null);
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
			@JsonProperty("usage") Usage usage) {

		@JsonInclude(Include.NON_NULL)
		public record Choice(
				@JsonProperty("finish_reason") ChatCompletionFinishReason finishReason,
				@JsonProperty("index") Integer index,
				@JsonProperty("message") ChatCompletionMessage message,
				@JsonProperty("logprobs") LogProbs logprobs) {

		}
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
	public record Embedding(
			@JsonProperty("index") Integer index,
			@JsonProperty("embedding") float[] embedding,
			@JsonProperty("object") String object) {

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
			return Objects.equals(this.index, embedding1.index) && Arrays.equals(this.embedding, embedding1.embedding) && Objects.equals(this.object, embedding1.object);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(this.index, this.object);
			result = 31 * result + Arrays.hashCode(this.embedding);
			return result;
		}

		@Override
		public String toString() {
			return "Embedding{" +
					"index=" + this.index +
					", embedding=" + Arrays.toString(this.embedding) +
					", object='" + this.object + '\'' +
					'}';
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingRequest<T>(
			@JsonProperty("input") T input,
			@JsonProperty("model") String model,
			@JsonProperty("dimensions") Integer dimensions) {

		public EmbeddingRequest(T input) {
			this(input, DEFAULT_EMBEDDING_MODEL, null);
		}

		public EmbeddingRequest(T input, String model) {
			this(input, model, null);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingList<T>(
			@JsonProperty("object") String object,
			@JsonProperty("data") List<T> data,
			@JsonProperty("model") String model,
			@JsonProperty("usage") Usage usage) {
	}

}
// @formatter:on
