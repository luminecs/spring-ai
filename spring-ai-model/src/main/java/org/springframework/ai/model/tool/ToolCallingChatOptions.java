package org.springframework.ai.model.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public interface ToolCallingChatOptions extends ChatOptions {

	boolean DEFAULT_TOOL_EXECUTION_ENABLED = true;

	List<ToolCallback> getToolCallbacks();

	void setToolCallbacks(List<ToolCallback> toolCallbacks);

	Set<String> getToolNames();

	void setToolNames(Set<String> toolNames);

	@Nullable
	Boolean getInternalToolExecutionEnabled();

	void setInternalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled);

	Map<String, Object> getToolContext();

	void setToolContext(Map<String, Object> toolContext);

	static Builder builder() {
		return new DefaultToolCallingChatOptions.Builder();
	}

	static boolean isInternalToolExecutionEnabled(ChatOptions chatOptions) {
		Assert.notNull(chatOptions, "chatOptions cannot be null");
		boolean internalToolExecutionEnabled;
		if (chatOptions instanceof ToolCallingChatOptions toolCallingChatOptions
				&& toolCallingChatOptions.getInternalToolExecutionEnabled() != null) {
			internalToolExecutionEnabled = Boolean.TRUE
				.equals(toolCallingChatOptions.getInternalToolExecutionEnabled());
		}
		else {
			internalToolExecutionEnabled = DEFAULT_TOOL_EXECUTION_ENABLED;
		}
		return internalToolExecutionEnabled;
	}

	static Set<String> mergeToolNames(Set<String> runtimeToolNames, Set<String> defaultToolNames) {
		Assert.notNull(runtimeToolNames, "runtimeToolNames cannot be null");
		Assert.notNull(defaultToolNames, "defaultToolNames cannot be null");
		if (CollectionUtils.isEmpty(runtimeToolNames)) {
			return new HashSet<>(defaultToolNames);
		}
		return new HashSet<>(runtimeToolNames);
	}

	static List<ToolCallback> mergeToolCallbacks(List<ToolCallback> runtimeToolCallbacks,
			List<ToolCallback> defaultToolCallbacks) {
		Assert.notNull(runtimeToolCallbacks, "runtimeToolCallbacks cannot be null");
		Assert.notNull(defaultToolCallbacks, "defaultToolCallbacks cannot be null");
		if (CollectionUtils.isEmpty(runtimeToolCallbacks)) {
			return new ArrayList<>(defaultToolCallbacks);
		}
		return new ArrayList<>(runtimeToolCallbacks);
	}

	static Map<String, Object> mergeToolContext(Map<String, Object> runtimeToolContext,
			Map<String, Object> defaultToolContext) {
		Assert.notNull(runtimeToolContext, "runtimeToolContext cannot be null");
		Assert.noNullElements(runtimeToolContext.keySet(), "runtimeToolContext keys cannot be null");
		Assert.notNull(defaultToolContext, "defaultToolContext cannot be null");
		Assert.noNullElements(defaultToolContext.keySet(), "defaultToolContext keys cannot be null");
		var mergedToolContext = new HashMap<>(defaultToolContext);
		mergedToolContext.putAll(runtimeToolContext);
		return mergedToolContext;
	}

	static void validateToolCallbacks(List<ToolCallback> toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException("Multiple tools with the same name (%s) found in ToolCallingChatOptions"
				.formatted(String.join(", ", duplicateToolNames)));
		}
	}

	interface Builder extends ChatOptions.Builder {

		Builder toolCallbacks(List<ToolCallback> toolCallbacks);

		Builder toolCallbacks(ToolCallback... toolCallbacks);

		Builder toolNames(Set<String> toolNames);

		Builder toolNames(String... toolNames);

		Builder internalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled);

		Builder toolContext(Map<String, Object> context);

		Builder toolContext(String key, Object value);

		@Override
		Builder model(@Nullable String model);

		@Override
		Builder frequencyPenalty(@Nullable Double frequencyPenalty);

		@Override
		Builder maxTokens(@Nullable Integer maxTokens);

		@Override
		Builder presencePenalty(@Nullable Double presencePenalty);

		@Override
		Builder stopSequences(@Nullable List<String> stopSequences);

		@Override
		Builder temperature(@Nullable Double temperature);

		@Override
		Builder topK(@Nullable Integer topK);

		@Override
		Builder topP(@Nullable Double topP);

		@Override
		ToolCallingChatOptions build();

	}

}
