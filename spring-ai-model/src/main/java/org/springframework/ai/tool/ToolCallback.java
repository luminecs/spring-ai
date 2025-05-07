package org.springframework.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.lang.Nullable;

public interface ToolCallback {

	ToolDefinition getToolDefinition();

	default ToolMetadata getToolMetadata() {
		return ToolMetadata.builder().build();
	}

	String call(String toolInput);

	default String call(String toolInput, @Nullable ToolContext tooContext) {
		if (tooContext != null && !tooContext.getContext().isEmpty()) {
			throw new UnsupportedOperationException("Tool context is not supported!");
		}
		return call(toolInput);
	}

}
