package org.springframework.ai.ollama;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaApi.Message.ToolCall;
import org.springframework.ai.ollama.api.OllamaApi.Message.ToolCallFunction;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.api.common.OllamaApiConstants;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class OllamaChatModel implements ChatModel {

	private static final Logger logger = LoggerFactory.getLogger(OllamaChatModel.class);

	private static final String DONE = "done";

	private static final String METADATA_PROMPT_EVAL_COUNT = "prompt-eval-count";

	private static final String METADATA_EVAL_COUNT = "eval-count";

	private static final String METADATA_CREATED_AT = "created-at";

	private static final String METADATA_TOTAL_DURATION = "total-duration";

	private static final String METADATA_LOAD_DURATION = "load-duration";

	private static final String METADATA_PROMPT_EVAL_DURATION = "prompt-eval-duration";

	private static final String METADATA_EVAL_DURATION = "eval-duration";

	private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

	private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

	private final OllamaApi chatApi;

	private final OllamaOptions defaultOptions;

	private final ObservationRegistry observationRegistry;

	private final OllamaModelManager modelManager;

	private final ToolCallingManager toolCallingManager;

	private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate;

	private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public OllamaChatModel(OllamaApi ollamaApi, OllamaOptions defaultOptions, ToolCallingManager toolCallingManager,
			ObservationRegistry observationRegistry, ModelManagementOptions modelManagementOptions) {
		this(ollamaApi, defaultOptions, toolCallingManager, observationRegistry, modelManagementOptions,
				new DefaultToolExecutionEligibilityPredicate());
	}

	public OllamaChatModel(OllamaApi ollamaApi, OllamaOptions defaultOptions, ToolCallingManager toolCallingManager,
			ObservationRegistry observationRegistry, ModelManagementOptions modelManagementOptions,
			ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
		Assert.notNull(ollamaApi, "ollamaApi must not be null");
		Assert.notNull(defaultOptions, "defaultOptions must not be null");
		Assert.notNull(toolCallingManager, "toolCallingManager must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		Assert.notNull(modelManagementOptions, "modelManagementOptions must not be null");
		Assert.notNull(toolExecutionEligibilityPredicate, "toolExecutionEligibilityPredicate must not be null");
		this.chatApi = ollamaApi;
		this.defaultOptions = defaultOptions;
		this.toolCallingManager = toolCallingManager;
		this.observationRegistry = observationRegistry;
		this.modelManager = new OllamaModelManager(this.chatApi, modelManagementOptions);
		this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
		initializeModel(defaultOptions.getModel(), modelManagementOptions.pullModelStrategy());
	}

	public static Builder builder() {
		return new Builder();
	}

	static ChatResponseMetadata from(OllamaApi.ChatResponse response, ChatResponse previousChatResponse) {
		Assert.notNull(response, "OllamaApi.ChatResponse must not be null");

		DefaultUsage newUsage = getDefaultUsage(response);
		Integer promptTokens = newUsage.getPromptTokens();
		Integer generationTokens = newUsage.getCompletionTokens();
		int totalTokens = newUsage.getTotalTokens();

		Duration evalDuration = response.getEvalDuration();
		Duration promptEvalDuration = response.getPromptEvalDuration();
		Duration loadDuration = response.getLoadDuration();
		Duration totalDuration = response.getTotalDuration();

		if (previousChatResponse != null && previousChatResponse.getMetadata() != null) {
			if (previousChatResponse.getMetadata().get(METADATA_EVAL_DURATION) != null) {
				evalDuration = evalDuration.plus(previousChatResponse.getMetadata().get(METADATA_EVAL_DURATION));
			}
			if (previousChatResponse.getMetadata().get(METADATA_PROMPT_EVAL_DURATION) != null) {
				promptEvalDuration = promptEvalDuration
					.plus(previousChatResponse.getMetadata().get(METADATA_PROMPT_EVAL_DURATION));
			}
			if (previousChatResponse.getMetadata().get(METADATA_LOAD_DURATION) != null) {
				loadDuration = loadDuration.plus(previousChatResponse.getMetadata().get(METADATA_LOAD_DURATION));
			}
			if (previousChatResponse.getMetadata().get(METADATA_TOTAL_DURATION) != null) {
				totalDuration = totalDuration.plus(previousChatResponse.getMetadata().get(METADATA_TOTAL_DURATION));
			}
			if (previousChatResponse.getMetadata().getUsage() != null) {
				promptTokens += previousChatResponse.getMetadata().getUsage().getPromptTokens();
				generationTokens += previousChatResponse.getMetadata().getUsage().getCompletionTokens();
				totalTokens += previousChatResponse.getMetadata().getUsage().getTotalTokens();
			}
		}

		DefaultUsage aggregatedUsage = new DefaultUsage(promptTokens, generationTokens, totalTokens);

		return ChatResponseMetadata.builder()
			.usage(aggregatedUsage)
			.model(response.model())
			.keyValue(METADATA_CREATED_AT, response.createdAt())
			.keyValue(METADATA_EVAL_DURATION, evalDuration)
			.keyValue(METADATA_EVAL_COUNT, aggregatedUsage.getCompletionTokens().intValue())
			.keyValue(METADATA_LOAD_DURATION, loadDuration)
			.keyValue(METADATA_PROMPT_EVAL_DURATION, promptEvalDuration)
			.keyValue(METADATA_PROMPT_EVAL_COUNT, aggregatedUsage.getPromptTokens().intValue())
			.keyValue(METADATA_TOTAL_DURATION, totalDuration)
			.keyValue(DONE, response.done())
			.build();
	}

	private static DefaultUsage getDefaultUsage(OllamaApi.ChatResponse response) {
		return new DefaultUsage(Optional.ofNullable(response.promptEvalCount()).orElse(0),
				Optional.ofNullable(response.evalCount()).orElse(0));
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		Prompt requestPrompt = buildRequestPrompt(prompt);
		return this.internalCall(requestPrompt, null);
	}

	private ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {

		OllamaApi.ChatRequest request = ollamaChatRequest(prompt, false);

		ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
			.prompt(prompt)
			.provider(OllamaApiConstants.PROVIDER_NAME)
			.build();

		ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {

				OllamaApi.ChatResponse ollamaResponse = this.chatApi.chat(request);

				List<AssistantMessage.ToolCall> toolCalls = ollamaResponse.message().toolCalls() == null ? List.of()
						: ollamaResponse.message()
							.toolCalls()
							.stream()
							.map(toolCall -> new AssistantMessage.ToolCall("", "function", toolCall.function().name(),
									ModelOptionsUtils.toJsonString(toolCall.function().arguments())))
							.toList();

				var assistantMessage = new AssistantMessage(ollamaResponse.message().content(), Map.of(), toolCalls);

				ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.NULL;
				if (ollamaResponse.promptEvalCount() != null && ollamaResponse.evalCount() != null) {
					generationMetadata = ChatGenerationMetadata.builder()
						.finishReason(ollamaResponse.doneReason())
						.build();
				}

				var generator = new Generation(assistantMessage, generationMetadata);
				ChatResponse chatResponse = new ChatResponse(List.of(generator),
						from(ollamaResponse, previousChatResponse));

				observationContext.setResponse(chatResponse);

				return chatResponse;

			});

		if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
			var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
			if (toolExecutionResult.returnDirect()) {

				return ChatResponse.builder()
					.from(response)
					.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
					.build();
			}
			else {

				return this.internalCall(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
						response);
			}
		}

		return response;
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {

		Prompt requestPrompt = buildRequestPrompt(prompt);
		return this.internalStream(requestPrompt, null);
	}

	private Flux<ChatResponse> internalStream(Prompt prompt, ChatResponse previousChatResponse) {
		return Flux.deferContextual(contextView -> {
			OllamaApi.ChatRequest request = ollamaChatRequest(prompt, true);

			final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
				.prompt(prompt)
				.provider(OllamaApiConstants.PROVIDER_NAME)
				.build();

			Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
					this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry);

			observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

			Flux<OllamaApi.ChatResponse> ollamaResponse = this.chatApi.streamingChat(request);

			Flux<ChatResponse> chatResponse = ollamaResponse.map(chunk -> {
				String content = (chunk.message() != null) ? chunk.message().content() : "";

				List<AssistantMessage.ToolCall> toolCalls = List.of();

				if (chunk.message() != null && chunk.message().toolCalls() != null) {
					toolCalls = chunk.message()
						.toolCalls()
						.stream()
						.map(toolCall -> new AssistantMessage.ToolCall("", "function", toolCall.function().name(),
								ModelOptionsUtils.toJsonString(toolCall.function().arguments())))
						.toList();
				}

				var assistantMessage = new AssistantMessage(content, Map.of(), toolCalls);

				ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.NULL;
				if (chunk.promptEvalCount() != null && chunk.evalCount() != null) {
					generationMetadata = ChatGenerationMetadata.builder().finishReason(chunk.doneReason()).build();
				}

				var generator = new Generation(assistantMessage, generationMetadata);
				return new ChatResponse(List.of(generator), from(chunk, previousChatResponse));
			});

			// @formatter:off
			Flux<ChatResponse> chatResponseFlux = chatResponse.flatMap(response -> {
				if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {

					return Flux.defer(() -> {
						var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
						if (toolExecutionResult.returnDirect()) {

							return Flux.just(ChatResponse.builder().from(response)
									.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
									.build());
						} else {

							return this.internalStream(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
									response);
						}
					}).subscribeOn(Schedulers.boundedElastic());
				}
				else {
					return Flux.just(response);
				}
			})
			.doOnError(observation::error)
			.doFinally(s ->
				observation.stop()
			)
			.contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));
			// @formatter:on

			return new MessageAggregator().aggregate(chatResponseFlux, observationContext::setResponse);
		});
	}

	Prompt buildRequestPrompt(Prompt prompt) {

		OllamaOptions runtimeOptions = null;
		if (prompt.getOptions() != null) {
			if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
				runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
						OllamaOptions.class);
			}
			else {
				runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
						OllamaOptions.class);
			}
		}

		OllamaOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
				OllamaOptions.class);

		if (runtimeOptions != null) {
			requestOptions.setInternalToolExecutionEnabled(
					ModelOptionsUtils.mergeOption(runtimeOptions.getInternalToolExecutionEnabled(),
							this.defaultOptions.getInternalToolExecutionEnabled()));
			requestOptions.setToolNames(ToolCallingChatOptions.mergeToolNames(runtimeOptions.getToolNames(),
					this.defaultOptions.getToolNames()));
			requestOptions.setToolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(runtimeOptions.getToolCallbacks(),
					this.defaultOptions.getToolCallbacks()));
			requestOptions.setToolContext(ToolCallingChatOptions.mergeToolContext(runtimeOptions.getToolContext(),
					this.defaultOptions.getToolContext()));
		}
		else {
			requestOptions.setInternalToolExecutionEnabled(this.defaultOptions.getInternalToolExecutionEnabled());
			requestOptions.setToolNames(this.defaultOptions.getToolNames());
			requestOptions.setToolCallbacks(this.defaultOptions.getToolCallbacks());
			requestOptions.setToolContext(this.defaultOptions.getToolContext());
		}

		if (!StringUtils.hasText(requestOptions.getModel())) {
			throw new IllegalArgumentException("model cannot be null or empty");
		}

		ToolCallingChatOptions.validateToolCallbacks(requestOptions.getToolCallbacks());

		return new Prompt(prompt.getInstructions(), requestOptions);
	}

	OllamaApi.ChatRequest ollamaChatRequest(Prompt prompt, boolean stream) {

		List<OllamaApi.Message> ollamaMessages = prompt.getInstructions().stream().map(message -> {
			if (message instanceof UserMessage userMessage) {
				var messageBuilder = OllamaApi.Message.builder(Role.USER).content(message.getText());
				if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
					messageBuilder.images(
							userMessage.getMedia().stream().map(media -> this.fromMediaData(media.getData())).toList());
				}
				return List.of(messageBuilder.build());
			}
			else if (message instanceof SystemMessage systemMessage) {
				return List.of(OllamaApi.Message.builder(Role.SYSTEM).content(systemMessage.getText()).build());
			}
			else if (message instanceof AssistantMessage assistantMessage) {
				List<ToolCall> toolCalls = null;
				if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
					toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
						var function = new ToolCallFunction(toolCall.name(),
								JsonParser.fromJson(toolCall.arguments(), new TypeReference<>() {
								}));
						return new ToolCall(function);
					}).toList();
				}
				return List.of(OllamaApi.Message.builder(Role.ASSISTANT)
					.content(assistantMessage.getText())
					.toolCalls(toolCalls)
					.build());
			}
			else if (message instanceof ToolResponseMessage toolMessage) {
				return toolMessage.getResponses()
					.stream()
					.map(tr -> OllamaApi.Message.builder(Role.TOOL).content(tr.responseData()).build())
					.toList();
			}
			throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
		}).flatMap(List::stream).toList();

		OllamaOptions requestOptions = (OllamaOptions) prompt.getOptions();

		OllamaApi.ChatRequest.Builder requestBuilder = OllamaApi.ChatRequest.builder(requestOptions.getModel())
			.stream(stream)
			.messages(ollamaMessages)
			.options(requestOptions);

		if (requestOptions.getFormat() != null) {
			requestBuilder.format(requestOptions.getFormat());
		}

		if (requestOptions.getKeepAlive() != null) {
			requestBuilder.keepAlive(requestOptions.getKeepAlive());
		}

		List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);
		if (!CollectionUtils.isEmpty(toolDefinitions)) {
			requestBuilder.tools(this.getTools(toolDefinitions));
		}

		return requestBuilder.build();
	}

	private String fromMediaData(Object mediaData) {
		if (mediaData instanceof byte[] bytes) {
			return Base64.getEncoder().encodeToString(bytes);
		}
		else if (mediaData instanceof String text) {
			return text;
		}
		else {
			throw new IllegalArgumentException("Unsupported media data type: " + mediaData.getClass().getSimpleName());
		}

	}

	private List<ChatRequest.Tool> getTools(List<ToolDefinition> toolDefinitions) {
		return toolDefinitions.stream().map(toolDefinition -> {
			var tool = new ChatRequest.Tool.Function(toolDefinition.name(), toolDefinition.description(),
					toolDefinition.inputSchema());
			return new ChatRequest.Tool(tool);
		}).toList();
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return OllamaOptions.fromOptions(this.defaultOptions);
	}

	private void initializeModel(String model, PullModelStrategy pullModelStrategy) {
		if (pullModelStrategy != null && !PullModelStrategy.NEVER.equals(pullModelStrategy)) {
			this.modelManager.pullModel(model, pullModelStrategy);
		}
	}

	public void setObservationConvention(ChatModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention cannot be null");
		this.observationConvention = observationConvention;
	}

	public static final class Builder {

		private OllamaApi ollamaApi;

		private OllamaOptions defaultOptions = OllamaOptions.builder().model(OllamaModel.MISTRAL.id()).build();

		private ToolCallingManager toolCallingManager;

		private ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = new DefaultToolExecutionEligibilityPredicate();

		private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

		private ModelManagementOptions modelManagementOptions = ModelManagementOptions.defaults();

		private Builder() {
		}

		public Builder ollamaApi(OllamaApi ollamaApi) {
			this.ollamaApi = ollamaApi;
			return this;
		}

		public Builder defaultOptions(OllamaOptions defaultOptions) {
			this.defaultOptions = defaultOptions;
			return this;
		}

		public Builder toolCallingManager(ToolCallingManager toolCallingManager) {
			this.toolCallingManager = toolCallingManager;
			return this;
		}

		public Builder toolExecutionEligibilityPredicate(
				ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
			this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
			return this;
		}

		public Builder observationRegistry(ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}

		public Builder modelManagementOptions(ModelManagementOptions modelManagementOptions) {
			this.modelManagementOptions = modelManagementOptions;
			return this;
		}

		public OllamaChatModel build() {
			if (this.toolCallingManager != null) {
				return new OllamaChatModel(this.ollamaApi, this.defaultOptions, this.toolCallingManager,
						this.observationRegistry, this.modelManagementOptions, this.toolExecutionEligibilityPredicate);
			}
			return new OllamaChatModel(this.ollamaApi, this.defaultOptions, DEFAULT_TOOL_CALLING_MANAGER,
					this.observationRegistry, this.modelManagementOptions, this.toolExecutionEligibilityPredicate);
		}

	}

}
