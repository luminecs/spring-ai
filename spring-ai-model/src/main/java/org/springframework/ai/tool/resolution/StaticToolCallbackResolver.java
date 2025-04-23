package org.springframework.ai.tool.resolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

public class StaticToolCallbackResolver implements ToolCallbackResolver {

	private static final Logger logger = LoggerFactory.getLogger(StaticToolCallbackResolver.class);

	private final Map<String, ToolCallback> toolCallbacks = new HashMap<>();

	public StaticToolCallbackResolver(List<ToolCallback> toolCallbacks) {
		Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");

		toolCallbacks
			.forEach(toolCallback -> this.toolCallbacks.put(toolCallback.getToolDefinition().name(), toolCallback));
	}

	@Override
	public ToolCallback resolve(String toolName) {
		Assert.hasText(toolName, "toolName cannot be null or empty");
		logger.debug("ToolCallback resolution attempt from static registry");
		return this.toolCallbacks.get(toolName);
	}

}
