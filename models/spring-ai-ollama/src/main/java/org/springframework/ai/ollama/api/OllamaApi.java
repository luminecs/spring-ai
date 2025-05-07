package org.springframework.ai.ollama.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.ollama.api.common.OllamaApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

// @formatter:off
public class OllamaApi {

	public static Builder builder() { return new Builder(); }

	public static final String REQUEST_BODY_NULL_ERROR = "The request body can not be null.";

	private static final Log logger = LogFactory.getLog(OllamaApi.class);

	private final RestClient restClient;

	private final WebClient webClient;

	private OllamaApi(String baseUrl, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {

		Consumer<HttpHeaders> defaultHeaders = headers -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		};

		this.restClient = restClientBuilder.baseUrl(baseUrl)
				.defaultHeaders(defaultHeaders)
				.defaultStatusHandler(responseErrorHandler)
				.build();

		this.webClient = webClientBuilder
				.baseUrl(baseUrl)
				.defaultHeaders(defaultHeaders)
				.build();
	}

	public ChatResponse chat(ChatRequest chatRequest) {
		Assert.notNull(chatRequest, REQUEST_BODY_NULL_ERROR);
		Assert.isTrue(!chatRequest.stream(), "Stream mode must be disabled.");

		return this.restClient.post()
			.uri("/api/chat")
			.body(chatRequest)
			.retrieve()
			.body(ChatResponse.class);
	}

	public Flux<ChatResponse> streamingChat(ChatRequest chatRequest) {
		Assert.notNull(chatRequest, REQUEST_BODY_NULL_ERROR);
		Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

		AtomicBoolean isInsideTool = new AtomicBoolean(false);

		return this.webClient.post()
			.uri("/api/chat")
			.body(Mono.just(chatRequest), ChatRequest.class)
			.retrieve()
			.bodyToFlux(ChatResponse.class)
			.map(chunk -> {
				if (OllamaApiHelper.isStreamingToolCall(chunk)) {
					isInsideTool.set(true);
				}
				return chunk;
			})

			.windowUntil(chunk -> {
				if (isInsideTool.get() && OllamaApiHelper.isStreamingDone(chunk)) {
					isInsideTool.set(false);
					return true;
				}
				return !isInsideTool.get();
			})

			.concatMapIterable(window -> {
				Mono<ChatResponse> monoChunk = window.reduce(
						new ChatResponse(),
						(previous, current) -> OllamaApiHelper.merge(previous, current));
				return List.of(monoChunk);
			})

			.flatMap(mono -> mono)
			.handle((data, sink) -> {
				if (logger.isTraceEnabled()) {
					logger.trace(data);
				}
				sink.next(data);
			});
	}

	public EmbeddingsResponse embed(EmbeddingsRequest embeddingsRequest) {
		Assert.notNull(embeddingsRequest, REQUEST_BODY_NULL_ERROR);

		return this.restClient.post()
			.uri("/api/embed")
			.body(embeddingsRequest)
			.retrieve()
			.body(EmbeddingsResponse.class);
	}

	public ListModelResponse listModels() {
		return this.restClient.get()
				.uri("/api/tags")
				.retrieve()
				.body(ListModelResponse.class);
	}

	public ShowModelResponse showModel(ShowModelRequest showModelRequest) {
		Assert.notNull(showModelRequest, "showModelRequest must not be null");
		return this.restClient.post()
				.uri("/api/show")
				.body(showModelRequest)
				.retrieve()
				.body(ShowModelResponse.class);
	}

	public ResponseEntity<Void> copyModel(CopyModelRequest copyModelRequest) {
		Assert.notNull(copyModelRequest, "copyModelRequest must not be null");
		return this.restClient.post()
				.uri("/api/copy")
				.body(copyModelRequest)
				.retrieve()
				.toBodilessEntity();
	}

	public ResponseEntity<Void> deleteModel(DeleteModelRequest deleteModelRequest) {
		Assert.notNull(deleteModelRequest, "deleteModelRequest must not be null");
		return this.restClient.method(HttpMethod.DELETE)
				.uri("/api/delete")
				.body(deleteModelRequest)
				.retrieve()
				.toBodilessEntity();
	}

	public Flux<ProgressResponse> pullModel(PullModelRequest pullModelRequest) {
		Assert.notNull(pullModelRequest, "pullModelRequest must not be null");
		Assert.isTrue(pullModelRequest.stream(), "Request must set the stream property to true.");

		return this.webClient.post()
				.uri("/api/pull")
				.bodyValue(pullModelRequest)
				.retrieve()
				.bodyToFlux(ProgressResponse.class);
	}

	@JsonInclude(Include.NON_NULL)
	public record Message(
			@JsonProperty("role") Role role,
			@JsonProperty("content") String content,
			@JsonProperty("images") List<String> images,
			@JsonProperty("tool_calls") List<ToolCall> toolCalls) {

		public static Builder builder(Role role) {
			return new Builder(role);
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
		public record ToolCall(
			@JsonProperty("function") ToolCallFunction function) {
		}

		@JsonInclude(Include.NON_NULL)
		public record ToolCallFunction(
			@JsonProperty("name") String name,
			@JsonProperty("arguments") Map<String, Object> arguments) {
		}

		public static class Builder {

			private final Role role;
			private String content;
			private List<String> images;
			private List<ToolCall> toolCalls;

			public Builder(Role role) {
				this.role = role;
			}

			public Builder content(String content) {
				this.content = content;
				return this;
			}

			public Builder images(List<String> images) {
				this.images = images;
				return this;
			}

			public Builder toolCalls(List<ToolCall> toolCalls) {
				this.toolCalls = toolCalls;
				return this;
			}

			public Message build() {
				return new Message(this.role, this.content, this.images, this.toolCalls);
			}
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatRequest(
			@JsonProperty("model") String model,
			@JsonProperty("messages") List<Message> messages,
			@JsonProperty("stream") Boolean stream,
			@JsonProperty("format") Object format,
			@JsonProperty("keep_alive") String keepAlive,
			@JsonProperty("tools") List<Tool> tools,
			@JsonProperty("options") Map<String, Object> options
	) {

		public static Builder builder(String model) {
			return new Builder(model);
		}

		@JsonInclude(Include.NON_NULL)
		public record Tool(
				@JsonProperty("type") Type type,
				@JsonProperty("function") Function function) {

			public Tool(Function function) {
				this(Type.FUNCTION, function);
			}

			public enum Type {

				@JsonProperty("function")
				FUNCTION
			}

			public record Function(
				@JsonProperty("name") String name,
				@JsonProperty("description") String description,
				@JsonProperty("parameters") Map<String, Object> parameters) {

				public Function(String description, String name, String jsonSchema) {
					this(description, name, ModelOptionsUtils.jsonToMap(jsonSchema));
				}
			}
		}

		public static class Builder {

			private final String model;
			private List<Message> messages = List.of();
			private boolean stream = false;
			private Object format;
			private String keepAlive;
			private List<Tool> tools = List.of();
			private Map<String, Object> options = Map.of();

			public Builder(String model) {
				Assert.notNull(model, "The model can not be null.");
				this.model = model;
			}

			public Builder messages(List<Message> messages) {
				this.messages = messages;
				return this;
			}

			public Builder stream(boolean stream) {
				this.stream = stream;
				return this;
			}

			public Builder format(Object format) {
				this.format = format;
				return this;
			}

			public Builder keepAlive(String keepAlive) {
				this.keepAlive = keepAlive;
				return this;
			}

			public Builder tools(List<Tool> tools) {
				this.tools = tools;
				return this;
			}

			public Builder options(Map<String, Object> options) {
				Objects.requireNonNull(options, "The options can not be null.");

				this.options = OllamaOptions.filterNonSupportedFields(options);
				return this;
			}

			public Builder options(OllamaOptions options) {
				Objects.requireNonNull(options, "The options can not be null.");
				this.options = OllamaOptions.filterNonSupportedFields(options.toMap());
				return this;
			}

			public ChatRequest build() {
				return new ChatRequest(this.model, this.messages, this.stream, this.format, this.keepAlive, this.tools, this.options);
			}
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ChatResponse(
			@JsonProperty("model") String model,
			@JsonProperty("created_at") Instant createdAt,
			@JsonProperty("message") Message message,
			@JsonProperty("done_reason") String doneReason,
			@JsonProperty("done") Boolean done,
			@JsonProperty("total_duration") Long totalDuration,
			@JsonProperty("load_duration") Long loadDuration,
			@JsonProperty("prompt_eval_count") Integer promptEvalCount,
			@JsonProperty("prompt_eval_duration") Long promptEvalDuration,
			@JsonProperty("eval_count") Integer evalCount,
			@JsonProperty("eval_duration") Long evalDuration
	) {
		ChatResponse() {
			this(null, null, null, null, null, null, null, null, null, null, null);
		}

		public Duration getTotalDuration() {
			return (this.totalDuration() != null) ? Duration.ofNanos(this.totalDuration()) : null;
		}

		public Duration getLoadDuration() {
			return (this.loadDuration() != null) ? Duration.ofNanos(this.loadDuration()) : null;
		}

		public Duration getPromptEvalDuration() {
			return (this.promptEvalDuration() != null) ? Duration.ofNanos(this.promptEvalDuration()) : null;
		}

		public Duration getEvalDuration() {
			if (this.evalDuration() == null) {
				return null;
			}
			return Duration.ofNanos(this.evalDuration());

		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingsRequest(
			@JsonProperty("model") String model,
			@JsonProperty("input") List<String> input,
			@JsonProperty("keep_alive") Duration keepAlive,
			@JsonProperty("options") Map<String, Object> options,
			@JsonProperty("truncate") Boolean truncate) {

		public EmbeddingsRequest(String model, String input) {
			this(model, List.of(input), null, null, null);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record EmbeddingsResponse(
			@JsonProperty("model") String model,
			@JsonProperty("embeddings") List<float[]> embeddings,
			@JsonProperty("total_duration") Long totalDuration,
			@JsonProperty("load_duration") Long loadDuration,
			@JsonProperty("prompt_eval_count") Integer promptEvalCount) {

	}

	@JsonInclude(Include.NON_NULL)
	public record Model(
			@JsonProperty("name") String name,
			@JsonProperty("model") String model,
			@JsonProperty("modified_at") Instant modifiedAt,
			@JsonProperty("size") Long size,
			@JsonProperty("digest") String digest,
			@JsonProperty("details") Details details
	) {
		@JsonInclude(Include.NON_NULL)
		public record Details(
				@JsonProperty("parent_model") String parentModel,
				@JsonProperty("format") String format,
				@JsonProperty("family") String family,
				@JsonProperty("families") List<String> families,
				@JsonProperty("parameter_size") String parameterSize,
				@JsonProperty("quantization_level") String quantizationLevel
		) { }
	}

	@JsonInclude(Include.NON_NULL)
	public record ListModelResponse(
			@JsonProperty("models") List<Model> models
	) { }

	@JsonInclude(Include.NON_NULL)
	public record ShowModelRequest(
			@JsonProperty("model") String model,
			@JsonProperty("system") String system,
			@JsonProperty("verbose") Boolean verbose,
			@JsonProperty("options") Map<String, Object> options
	) {
		public ShowModelRequest(String model) {
			this(model, null, null, null);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ShowModelResponse(
			@JsonProperty("license") String license,
			@JsonProperty("modelfile") String modelfile,
			@JsonProperty("parameters") String parameters,
			@JsonProperty("template") String template,
			@JsonProperty("system") String system,
			@JsonProperty("details") Model.Details details,
			@JsonProperty("messages") List<Message> messages,
			@JsonProperty("model_info") Map<String, Object> modelInfo,
			@JsonProperty("projector_info") Map<String, Object> projectorInfo,
			@JsonProperty("capabilities") List<String> capabilities,
			@JsonProperty("modified_at") Instant modifiedAt
	) { }

	@JsonInclude(Include.NON_NULL)
	public record CopyModelRequest(
			@JsonProperty("source") String source,
			@JsonProperty("destination") String destination
	) { }

	@JsonInclude(Include.NON_NULL)
	public record DeleteModelRequest(
			@JsonProperty("model") String model
	) { }

	@JsonInclude(Include.NON_NULL)
	public record PullModelRequest(
			@JsonProperty("model") String model,
			@JsonProperty("insecure") boolean insecure,
			@JsonProperty("username") String username,
			@JsonProperty("password") String password,
			@JsonProperty("stream") boolean stream
	) {
		public PullModelRequest {
			if (!stream) {
				logger.warn("Enforcing streaming of the model pull request");
			}
			stream = true;
		}

		public PullModelRequest(String model) {
			this(model, false, null, null, true);
		}
	}

	@JsonInclude(Include.NON_NULL)
	public record ProgressResponse(
			@JsonProperty("status") String status,
			@JsonProperty("digest") String digest,
			@JsonProperty("total") Long total,
			@JsonProperty("completed") Long completed
	) { }

	public static class Builder {

		private String baseUrl = OllamaApiConstants.DEFAULT_BASE_URL;

		private RestClient.Builder restClientBuilder = RestClient.builder();

		private WebClient.Builder webClientBuilder = WebClient.builder();

		private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

		public Builder baseUrl(String baseUrl) {
			Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
			this.baseUrl = baseUrl;
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

		public OllamaApi build() {
			return new OllamaApi(this.baseUrl, this.restClientBuilder, this.webClientBuilder, this.responseErrorHandler);
		}

	}
}
// @formatter:on
