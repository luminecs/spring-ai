package org.springframework.ai.model.tool;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;

public class DefaultToolExecutionEligibilityPredicate implements ToolExecutionEligibilityPredicate {

	@Override
	public boolean test(ChatOptions promptOptions, ChatResponse chatResponse) {
		return ToolCallingChatOptions.isInternalToolExecutionEnabled(promptOptions) && chatResponse != null
				&& chatResponse.hasToolCalls();
	}

}
