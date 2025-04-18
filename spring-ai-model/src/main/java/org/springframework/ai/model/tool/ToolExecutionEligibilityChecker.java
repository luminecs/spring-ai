package org.springframework.ai.model.tool;

import java.util.function.Function;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.util.Assert;

public interface ToolExecutionEligibilityChecker extends Function<ChatResponse, Boolean> {

	default boolean isToolExecutionRequired(ChatOptions promptOptions, ChatResponse chatResponse) {
		Assert.notNull(promptOptions, "promptOptions cannot be null");
		Assert.notNull(chatResponse, "chatResponse cannot be null");
		return this.isInternalToolExecutionEnabled(promptOptions) && this.isToolCallResponse(chatResponse);
	}

	default boolean isToolCallResponse(ChatResponse chatResponse) {
		Assert.notNull(chatResponse, "chatResponse cannot be null");
		return apply(chatResponse);
	}

	default boolean isInternalToolExecutionEnabled(ChatOptions chatOptions) {

		Assert.notNull(chatOptions, "chatOptions cannot be null");
		boolean internalToolExecutionEnabled;
		if (chatOptions instanceof ToolCallingChatOptions toolCallingChatOptions
				&& toolCallingChatOptions.getInternalToolExecutionEnabled() != null) {
			internalToolExecutionEnabled = Boolean.TRUE
				.equals(toolCallingChatOptions.getInternalToolExecutionEnabled());
		}
		else if (chatOptions instanceof FunctionCallingOptions functionCallingOptions
				&& functionCallingOptions.getProxyToolCalls() != null) {
			internalToolExecutionEnabled = Boolean.TRUE.equals(!functionCallingOptions.getProxyToolCalls());
		}
		else {
			internalToolExecutionEnabled = true;
		}
		return internalToolExecutionEnabled;
	}

}
