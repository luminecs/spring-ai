package org.springframework.ai.model.tool;

import java.util.function.BiPredicate;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.util.Assert;

public interface ToolExecutionEligibilityPredicate extends BiPredicate<ChatOptions, ChatResponse> {

	default boolean isToolExecutionRequired(ChatOptions promptOptions, ChatResponse chatResponse) {
		Assert.notNull(promptOptions, "promptOptions cannot be null");
		Assert.notNull(chatResponse, "chatResponse cannot be null");
		return test(promptOptions, chatResponse);
	}

}
