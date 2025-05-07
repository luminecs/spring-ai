package org.springframework.ai.zhipuai;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.model.StreamingChatModel;
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
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletion;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletion.Choice;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionChunk;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage.ChatCompletionFunction;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage.MediaContent;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage.Role;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage.ToolCall;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionRequest;
import org.springframework.ai.zhipuai.api.ZhiPuApiConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;

public class ZhiPuAiChatModel implements ChatModel {

	private static final Logger logger = LoggerFactory.getLogger(ZhiPuAiChatModel.class);

	private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

	public final RetryTemplate retryTemplate;

	private final ZhiPuAiChatOptions defaultOptions;

	private final ZhiPuAiApi zhiPuAiApi;

	private final ObservationRegistry observationRegistry;

	private final ToolCallingManager toolCallingManager;

	private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate;

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi) {
		this(zhiPuAiApi, ZhiPuAiChatOptions.builder().model(ZhiPuAiApi.DEFAULT_CHAT_MODEL).temperature(0.7).build());
	}

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi, ZhiPuAiChatOptions options) {
		this(zhiPuAiApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi, ZhiPuAiChatOptions options, RetryTemplate retryTemplate) {
		this(zhiPuAiApi, options, ToolCallingManager.builder().build(), retryTemplate, ObservationRegistry.NOOP,
				new DefaultToolExecutionEligibilityPredicate());
	}

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi, ZhiPuAiChatOptions options, RetryTemplate retryTemplate,
			ObservationRegistry observationRegistry) {
		this(zhiPuAiApi, options, ToolCallingManager.builder().build(), retryTemplate, observationRegistry,
				new DefaultToolExecutionEligibilityPredicate());
	}

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi, ZhiPuAiChatOptions options, ToolCallingManager toolCallingManager,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
		this(zhiPuAiApi, options, toolCallingManager, retryTemplate, observationRegistry,
				new DefaultToolExecutionEligibilityPredicate());
	}

	public ZhiPuAiChatModel(ZhiPuAiApi zhiPuAiApi, ZhiPuAiChatOptions options, ToolCallingManager toolCallingManager,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry,
			ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
		Assert.notNull(zhiPuAiApi, "ZhiPuAiApi must not be null");
		Assert.notNull(options, "Options must not be null");
		Assert.notNull(retryTemplate, "RetryTemplate must not be null");
		Assert.notNull(observationRegistry, "ObservationRegistry must not be null");
		Assert.notNull(toolCallingManager, "toolCallingManager cannot be null");
		Assert.notNull(toolExecutionEligibilityPredicate, "toolExecutionEligibilityPredicate cannot be null");
		this.zhiPuAiApi = zhiPuAiApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
		this.toolCallingManager = toolCallingManager;
		this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
	}

	private static Generation buildGeneration(Choice choice, Map<String, Object> metadata) {
		List<AssistantMessage.ToolCall> toolCalls = choice.message().toolCalls() == null ? List.of()
				: choice.message()
					.toolCalls()
					.stream()
					.map(toolCall -> new AssistantMessage.ToolCall(toolCall.id(), "function",
							toolCall.function().name(), toolCall.function().arguments()))
					.toList();

		var assistantMessage = new AssistantMessage(choice.message().content(), metadata, toolCalls);
		String finishReason = (choice.finishReason() != null ? choice.finishReason().name() : "");
		var generationMetadata = ChatGenerationMetadata.builder().finishReason(finishReason).build();
		return new Generation(assistantMessage, generationMetadata);
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		Prompt requestPrompt = buildRequestPrompt(prompt);
		ChatCompletionRequest request = createRequest(requestPrompt, false);

		ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
			.prompt(requestPrompt)
			.provider(ZhiPuApiConstants.PROVIDER_NAME)
			.build();

		ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {

				ResponseEntity<ChatCompletion> completionEntity = this.retryTemplate
					.execute(ctx -> this.zhiPuAiApi.chatCompletionEntity(request));

				var chatCompletion = completionEntity.getBody();

				if (chatCompletion == null) {
					logger.warn("No chat completion returned for prompt: {}", prompt);
					return new ChatResponse(List.of());
				}

				List<Choice> choices = chatCompletion.choices();

				List<Generation> generations = choices.stream().map(choice -> {
			// @formatter:off
					Map<String, Object> metadata = Map.of(
						"id", chatCompletion.id(),
						"role", choice.message().role() != null ? choice.message().role().name() : "",
						"finishReason", choice.finishReason() != null ? choice.finishReason().name() : ""
					);
					// @formatter:on
					return buildGeneration(choice, metadata);
				}).toList();

				ChatResponse chatResponse = new ChatResponse(generations, from(completionEntity.getBody()));

				observationContext.setResponse(chatResponse);

				return chatResponse;
			});
		if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(requestPrompt.getOptions(), response)) {
			var toolExecutionResult = this.toolCallingManager.executeToolCalls(requestPrompt, response);
			if (toolExecutionResult.returnDirect()) {

				return ChatResponse.builder()
					.from(response)
					.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
					.build();
			}
			else {

				return this.call(new Prompt(toolExecutionResult.conversationHistory(), requestPrompt.getOptions()));
			}
		}
		return response;
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return ZhiPuAiChatOptions.fromOptions(this.defaultOptions);
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		return Flux.deferContextual(contextView -> {

			Prompt requestPrompt = buildRequestPrompt(prompt);
			ChatCompletionRequest request = createRequest(requestPrompt, true);

			Flux<ChatCompletionChunk> completionChunks = this.retryTemplate
				.execute(ctx -> this.zhiPuAiApi.chatCompletionStream(request));

			ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

			final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
				.prompt(requestPrompt)
				.provider(ZhiPuApiConstants.PROVIDER_NAME)
				.build();

			Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
					this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry);

			observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

			Flux<ChatResponse> chatResponse = completionChunks.map(this::chunkToChatCompletion)
				.switchMap(chatCompletion -> Mono.just(chatCompletion).map(chatCompletion2 -> {
					try {
						String id = chatCompletion2.id();

				// @formatter:off
						List<Generation> generations = chatCompletion2.choices().stream().map(choice -> {
							if (choice.message().role() != null) {
								roleMap.putIfAbsent(id, choice.message().role().name());
							}
							Map<String, Object> metadata = Map.of(
								"id", chatCompletion2.id(),
								"role", roleMap.getOrDefault(id, ""),
								"finishReason", choice.finishReason() != null ? choice.finishReason().name() : ""
							);
							return buildGeneration(choice, metadata);
						}).toList();
						// @formatter:on

						return new ChatResponse(generations, from(chatCompletion2));
					}
					catch (Exception e) {
						logger.error("Error processing chat completion", e);
						return new ChatResponse(List.of());
					}

				}));

			// @formatter:off
			Flux<ChatResponse> flux = chatResponse.flatMap(response -> {
						if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(requestPrompt.getOptions(), response)) {
							return Flux.defer(() -> {

								var toolExecutionResult = this.toolCallingManager.executeToolCalls(requestPrompt, response);
								if (toolExecutionResult.returnDirect()) {

									return Flux.just(ChatResponse.builder().from(response)
											.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
											.build());
								}
								else {

									return this.stream(new Prompt(toolExecutionResult.conversationHistory(), requestPrompt.getOptions()));
								}
							}).subscribeOn(Schedulers.boundedElastic());
						}
						return Flux.just(response);
			})
			.doOnError(observation::error)
			.doFinally(s -> observation.stop())
			.contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));
			// @formatter:on

			return new MessageAggregator().aggregate(flux, observationContext::setResponse);
		});
	}

	private ChatResponseMetadata from(ChatCompletion result) {
		Assert.notNull(result, "ZhiPuAI ChatCompletionResult must not be null");
		return ChatResponseMetadata.builder()
			.id(result.id() != null ? result.id() : "")
			.usage(result.usage() != null ? getDefaultUsage(result.usage()) : new EmptyUsage())
			.model(result.model() != null ? result.model() : "")
			.keyValue("created", result.created() != null ? result.created() : 0L)
			.keyValue("system-fingerprint", result.systemFingerprint() != null ? result.systemFingerprint() : "")
			.build();
	}

	private DefaultUsage getDefaultUsage(ZhiPuAiApi.Usage usage) {
		return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
	}

	private ChatCompletion chunkToChatCompletion(ChatCompletionChunk chunk) {
		List<ChatCompletion.Choice> choices = chunk.choices().stream().map(cc -> {
			ChatCompletionMessage delta = cc.delta();
			if (delta == null) {
				delta = new ChatCompletionMessage("", Role.ASSISTANT);
			}
			return new ChatCompletion.Choice(cc.finishReason(), cc.index(), delta, cc.logprobs());
		}).toList();

		return new ChatCompletion(chunk.id(), choices, chunk.created(), chunk.model(), chunk.systemFingerprint(),
				"chat.completion", null);
	}

	private List<ZhiPuAiApi.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
		return toolDefinitions.stream().map(toolDefinition -> {
			var function = new ZhiPuAiApi.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(),
					toolDefinition.inputSchema());
			return new ZhiPuAiApi.FunctionTool(function);
		}).toList();
	}

	Prompt buildRequestPrompt(Prompt prompt) {

		ZhiPuAiChatOptions runtimeOptions = null;
		if (prompt.getOptions() != null) {
			if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
				runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
						ZhiPuAiChatOptions.class);
			}
			else {
				runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
						ZhiPuAiChatOptions.class);
			}
		}

		ZhiPuAiChatOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
				ZhiPuAiChatOptions.class);

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

		ToolCallingChatOptions.validateToolCallbacks(requestOptions.getToolCallbacks());

		return new Prompt(prompt.getInstructions(), requestOptions);
	}

	ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

		List<ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map(message -> {
			if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.SYSTEM) {
				Object content = message.getText();
				if (message instanceof UserMessage userMessage) {
					if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
						List<MediaContent> contentList = new ArrayList<>(List.of(new MediaContent(message.getText())));

						contentList.addAll(userMessage.getMedia()
							.stream()
							.map(media -> new MediaContent(new MediaContent.ImageUrl(
									this.fromMediaData(media.getMimeType(), media.getData()))))
							.toList());

						content = contentList;
					}
				}

				return List.of(new ChatCompletionMessage(content,
						ChatCompletionMessage.Role.valueOf(message.getMessageType().name())));
			}
			else if (message.getMessageType() == MessageType.ASSISTANT) {
				var assistantMessage = (AssistantMessage) message;
				List<ToolCall> toolCalls = null;
				if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
					toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
						var function = new ChatCompletionFunction(toolCall.name(), toolCall.arguments());
						return new ToolCall(toolCall.id(), toolCall.type(), function);
					}).toList();
				}
				return List.of(new ChatCompletionMessage(assistantMessage.getText(),
						ChatCompletionMessage.Role.ASSISTANT, null, null, toolCalls));
			}
			else if (message.getMessageType() == MessageType.TOOL) {
				ToolResponseMessage toolMessage = (ToolResponseMessage) message;

				toolMessage.getResponses()
					.forEach(response -> Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id"));

				return toolMessage.getResponses()
					.stream()
					.map(tr -> new ChatCompletionMessage(tr.responseData(), ChatCompletionMessage.Role.TOOL, tr.name(),
							tr.id(), null))
					.toList();
			}
			else {
				throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
			}
		}).flatMap(List::stream).toList();

		ChatCompletionRequest request = new ChatCompletionRequest(chatCompletionMessages, stream);

		if (prompt.getOptions() != null) {
			ZhiPuAiChatOptions updatedRuntimeOptions;
			if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
				updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions,
						ToolCallingChatOptions.class, ZhiPuAiChatOptions.class);
			}
			else {
				updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
						ZhiPuAiChatOptions.class);
			}

			request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, ChatCompletionRequest.class);
		}

		request = ModelOptionsUtils.merge(request, this.defaultOptions, ChatCompletionRequest.class);

		ZhiPuAiChatOptions requestOptions = (ZhiPuAiChatOptions) prompt.getOptions();

		List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);
		if (!CollectionUtils.isEmpty(toolDefinitions)) {
			request = ModelOptionsUtils.merge(
					ZhiPuAiChatOptions.builder().tools(this.getFunctionTools(toolDefinitions)).build(), request,
					ChatCompletionRequest.class);
		}
		return request;
	}

	private String fromMediaData(MimeType mimeType, Object mediaContentData) {
		if (mediaContentData instanceof byte[] bytes) {

			return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
		}
		else if (mediaContentData instanceof String text) {

			return text;
		}
		else {
			throw new IllegalArgumentException(
					"Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
		}
	}

	private ChatOptions buildRequestOptions(ZhiPuAiApi.ChatCompletionRequest request) {
		return ChatOptions.builder()
			.model(request.model())
			.maxTokens(request.maxTokens())
			.stopSequences(request.stop())
			.temperature(request.temperature())
			.topP(request.topP())
			.build();
	}

	public void setObservationConvention(ChatModelObservationConvention observationConvention) {
		this.observationConvention = observationConvention;
	}

}
