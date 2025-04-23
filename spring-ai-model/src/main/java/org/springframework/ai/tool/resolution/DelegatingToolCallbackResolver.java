package org.springframework.ai.tool.resolution;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class DelegatingToolCallbackResolver implements ToolCallbackResolver {

	private final List<ToolCallbackResolver> toolCallbackResolvers;

	public DelegatingToolCallbackResolver(List<ToolCallbackResolver> toolCallbackResolvers) {
		Assert.notNull(toolCallbackResolvers, "toolCallbackResolvers cannot be null");
		Assert.noNullElements(toolCallbackResolvers, "toolCallbackResolvers cannot contain null elements");
		this.toolCallbackResolvers = toolCallbackResolvers;
	}

	@Override
	@Nullable
	public ToolCallback resolve(String toolName) {
		Assert.hasText(toolName, "toolName cannot be null or empty");

		for (ToolCallbackResolver toolCallbackResolver : this.toolCallbackResolvers) {
			ToolCallback toolCallback = toolCallbackResolver.resolve(toolName);
			if (toolCallback != null) {
				return toolCallback;
			}
		}
		return null;
	}

}
