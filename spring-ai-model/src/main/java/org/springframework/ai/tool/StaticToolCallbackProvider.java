package org.springframework.ai.tool;

import java.util.List;

import org.springframework.util.Assert;

public class StaticToolCallbackProvider implements ToolCallbackProvider {

	private final ToolCallback[] toolCallbacks;

	public StaticToolCallbackProvider(ToolCallback... toolCallbacks) {
		Assert.notNull(toolCallbacks, "ToolCallbacks must not be null");
		this.toolCallbacks = toolCallbacks;
	}

	public StaticToolCallbackProvider(List<? extends ToolCallback> toolCallbacks) {
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
		this.toolCallbacks = toolCallbacks.toArray(new ToolCallback[0]);
	}

	@Override
	public ToolCallback[] getToolCallbacks() {
		return this.toolCallbacks;
	}

}
