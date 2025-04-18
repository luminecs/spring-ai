package org.springframework.ai.model.tool;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.definition.ToolDefinition;

public interface ToolCallingManager {

	List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions);

	ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse);

	static DefaultToolCallingManager.Builder builder() {
		return DefaultToolCallingManager.builder();
	}

}
