package org.springframework.ai.tool;

import java.util.List;

import org.springframework.ai.model.function.FunctionCallback;

public interface ToolCallbackProvider {

	FunctionCallback[] getToolCallbacks();

	static ToolCallbackProvider from(List<? extends FunctionCallback> toolCallbacks) {
		return new StaticToolCallbackProvider(toolCallbacks);
	}

	static ToolCallbackProvider from(FunctionCallback... toolCallbacks) {
		return new StaticToolCallbackProvider(toolCallbacks);
	}

}
