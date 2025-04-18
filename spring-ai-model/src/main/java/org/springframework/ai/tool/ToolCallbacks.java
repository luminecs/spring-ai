package org.springframework.ai.tool;

import org.springframework.ai.tool.method.MethodToolCallbackProvider;

public final class ToolCallbacks {

	private ToolCallbacks() {
	}

	public static ToolCallback[] from(Object... sources) {
		return MethodToolCallbackProvider.builder().toolObjects(sources).build().getToolCallbacks();
	}

}
