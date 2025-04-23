package org.springframework.ai.tool;

import java.util.List;

public interface ToolCallbackProvider {

	ToolCallback[] getToolCallbacks();

	static ToolCallbackProvider from(List<? extends ToolCallback> toolCallbacks) {
		return new StaticToolCallbackProvider(toolCallbacks);
	}

	static ToolCallbackProvider from(ToolCallback... toolCallbacks) {
		return new StaticToolCallbackProvider(toolCallbacks);
	}

}
