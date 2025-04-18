package org.springframework.ai.tool;

import java.util.List;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.util.Assert;

public class StaticToolCallbackProvider implements ToolCallbackProvider {

	private final FunctionCallback[] toolCallbacks;

	public StaticToolCallbackProvider(FunctionCallback... toolCallbacks) {
		Assert.notNull(toolCallbacks, "ToolCallbacks must not be null");
		this.toolCallbacks = toolCallbacks;
	}

	public StaticToolCallbackProvider(List<? extends FunctionCallback> toolCallbacks) {
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
		this.toolCallbacks = toolCallbacks.toArray(new FunctionCallback[0]);
	}

	@Override
	public FunctionCallback[] getToolCallbacks() {
		return this.toolCallbacks;
	}

}
