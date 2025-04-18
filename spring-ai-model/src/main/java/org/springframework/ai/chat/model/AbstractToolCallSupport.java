package org.springframework.ai.chat.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackResolver;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@Deprecated
public abstract class AbstractToolCallSupport {

	protected static final boolean IS_RUNTIME_CALL = true;

	protected final Map<String, FunctionCallback> functionCallbackRegister = new ConcurrentHashMap<>();

	protected final FunctionCallbackResolver functionCallbackResolver;

	@Deprecated
	protected AbstractToolCallSupport(FunctionCallbackResolver functionCallbackResolver) {
		this(functionCallbackResolver, FunctionCallingOptions.builder().build(), List.of());
	}

	@Deprecated
	protected AbstractToolCallSupport(FunctionCallbackResolver functionCallbackResolver,
			FunctionCallingOptions functionCallingOptions, List<FunctionCallback> toolFunctionCallbacks) {

		this.functionCallbackResolver = functionCallbackResolver;

		List<FunctionCallback> defaultFunctionCallbacks = merge(functionCallingOptions, toolFunctionCallbacks);

		if (!CollectionUtils.isEmpty(defaultFunctionCallbacks)) {
			this.functionCallbackRegister.putAll(defaultFunctionCallbacks.stream()
				.collect(ConcurrentHashMap::new, (m, v) -> m.put(v.getName(), v), ConcurrentHashMap::putAll));
		}
	}

	private static List<FunctionCallback> merge(FunctionCallingOptions functionOptions,
			List<FunctionCallback> toolFunctionCallbacks) {
		List<FunctionCallback> toolFunctionCallbacksCopy = new ArrayList<>();
		if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
			toolFunctionCallbacksCopy.addAll(toolFunctionCallbacks);
		}

		if (!CollectionUtils.isEmpty(functionOptions.getFunctionCallbacks())) {
			toolFunctionCallbacksCopy.addAll(functionOptions.getFunctionCallbacks());

			functionOptions.setFunctionCallbacks(List.of());
		}
		return toolFunctionCallbacksCopy;
	}

	@Deprecated
	public Map<String, FunctionCallback> getFunctionCallbackRegister() {
		return this.functionCallbackRegister;
	}

	@Deprecated
	protected Set<String> runtimeFunctionCallbackConfigurations(FunctionCallingOptions runtimeFunctionOptions) {

		Set<String> enabledFunctionsToCall = new HashSet<>();

		if (runtimeFunctionOptions != null) {

			if (!CollectionUtils.isEmpty(runtimeFunctionOptions.getFunctions())) {
				enabledFunctionsToCall.addAll(runtimeFunctionOptions.getFunctions());
			}

			if (!CollectionUtils.isEmpty(runtimeFunctionOptions.getFunctionCallbacks())) {
				runtimeFunctionOptions.getFunctionCallbacks().stream().forEach(functionCallback -> {

					this.functionCallbackRegister.put(functionCallback.getName(), functionCallback);

					enabledFunctionsToCall.add(functionCallback.getName());
				});
			}
		}

		return enabledFunctionsToCall;
	}

	@Deprecated
	protected List<Message> handleToolCalls(Prompt prompt, ChatResponse response) {
		Optional<Generation> toolCallGeneration = response.getResults()
			.stream()
			.filter(g -> !CollectionUtils.isEmpty(g.getOutput().getToolCalls()))
			.findFirst();
		if (toolCallGeneration.isEmpty()) {
			throw new IllegalStateException("No tool call generation found in the response!");
		}
		AssistantMessage assistantMessage = toolCallGeneration.get().getOutput();

		Map<String, Object> toolContextMap = Map.of();
		if (prompt.getOptions() instanceof FunctionCallingOptions functionCallOptions
				&& !CollectionUtils.isEmpty(functionCallOptions.getToolContext())) {

			toolContextMap = new HashMap<>(functionCallOptions.getToolContext());

			List<Message> toolCallHistory = new ArrayList<>(prompt.copy().getInstructions());
			toolCallHistory.add(new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(),
					assistantMessage.getToolCalls()));

			toolContextMap.put(ToolContext.TOOL_CALL_HISTORY, toolCallHistory);
		}

		ToolResponseMessage toolMessageResponse = this.executeFunctions(assistantMessage,
				new ToolContext(toolContextMap));

		List<Message> toolConversationHistory = this.buildToolCallConversation(prompt.getInstructions(),
				assistantMessage, toolMessageResponse);

		return toolConversationHistory;
	}

	@Deprecated
	protected List<Message> buildToolCallConversation(List<Message> previousMessages, AssistantMessage assistantMessage,
			ToolResponseMessage toolResponseMessage) {
		List<Message> messages = new ArrayList<>(previousMessages);
		messages.add(assistantMessage);
		messages.add(toolResponseMessage);
		return messages;
	}

	@Deprecated
	protected List<FunctionCallback> resolveFunctionCallbacks(Set<String> functionNames) {

		List<FunctionCallback> retrievedFunctionCallbacks = new ArrayList<>();

		for (String functionName : functionNames) {
			if (!this.functionCallbackRegister.containsKey(functionName)) {

				if (this.functionCallbackResolver != null) {
					FunctionCallback functionCallback = this.functionCallbackResolver.resolve(functionName);
					if (functionCallback != null) {
						this.functionCallbackRegister.put(functionName, functionCallback);
					}
					else {
						throw new IllegalStateException(
								"No function callback [" + functionName + "] found in tht FunctionCallbackRegister");
					}
				}
				else {
					throw new IllegalStateException("No function callback found for name: " + functionName);
				}
			}
			FunctionCallback functionCallback = this.functionCallbackRegister.get(functionName);

			retrievedFunctionCallbacks.add(functionCallback);
		}

		return retrievedFunctionCallbacks;
	}

	@Deprecated
	protected ToolResponseMessage executeFunctions(AssistantMessage assistantMessage, ToolContext toolContext) {

		List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

		for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {

			var functionName = toolCall.name();
			String functionArguments = toolCall.arguments();

			if (!this.functionCallbackRegister.containsKey(functionName)) {
				throw new IllegalStateException("No function callback found for function name: " + functionName);
			}

			String functionResponse = this.functionCallbackRegister.get(functionName)
				.call(functionArguments, toolContext);

			toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), functionName, functionResponse));
		}

		return new ToolResponseMessage(toolResponses, Map.of());
	}

	@Deprecated
	protected boolean isToolCall(ChatResponse chatResponse, Set<String> toolCallFinishReasons) {
		Assert.isTrue(!CollectionUtils.isEmpty(toolCallFinishReasons), "Tool call finish reasons cannot be empty!");

		if (chatResponse == null) {
			return false;
		}

		var generations = chatResponse.getResults();
		if (CollectionUtils.isEmpty(generations)) {
			return false;
		}

		return generations.stream().anyMatch(g -> isToolCall(g, toolCallFinishReasons));
	}

	@Deprecated
	protected boolean isToolCall(Generation generation, Set<String> toolCallFinishReasons) {
		var finishReason = (generation.getMetadata().getFinishReason() != null)
				? generation.getMetadata().getFinishReason() : "";
		return generation.getOutput().hasToolCalls() && toolCallFinishReasons.stream()
			.map(s -> s.toLowerCase())
			.toList()
			.contains(finishReason.toLowerCase());
	}

	@Deprecated
	protected boolean isProxyToolCalls(Prompt prompt, FunctionCallingOptions defaultOptions) {
		if (prompt.getOptions() instanceof FunctionCallingOptions functionCallOptions
				&& functionCallOptions.getProxyToolCalls() != null) {
			return functionCallOptions.getProxyToolCalls();
		}
		else if (defaultOptions.getProxyToolCalls() != null) {
			return defaultOptions.getProxyToolCalls();
		}

		return false;
	}

}
