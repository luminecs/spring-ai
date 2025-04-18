package org.springframework.ai.model.tool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.Generation;

public interface ToolExecutionResult {

	String FINISH_REASON = "returnDirect";

	String METADATA_TOOL_ID = "toolId";

	String METADATA_TOOL_NAME = "toolName";

	List<Message> conversationHistory();

	default boolean returnDirect() {
		return false;
	}

	static DefaultToolExecutionResult.Builder builder() {
		return DefaultToolExecutionResult.builder();
	}

	static List<Generation> buildGenerations(ToolExecutionResult toolExecutionResult) {
		List<Message> conversationHistory = toolExecutionResult.conversationHistory();
		List<Generation> generations = new ArrayList<>();
		if (conversationHistory
			.get(conversationHistory.size() - 1) instanceof ToolResponseMessage toolResponseMessage) {
			toolResponseMessage.getResponses().forEach(response -> {
				AssistantMessage assistantMessage = new AssistantMessage(response.responseData());
				Generation generation = new Generation(assistantMessage,
						ChatGenerationMetadata.builder()
							.metadata(METADATA_TOOL_ID, response.id())
							.metadata(METADATA_TOOL_NAME, response.name())
							.finishReason(FINISH_REASON)
							.build());
				generations.add(generation);
			});
		}
		return generations;
	}

}
