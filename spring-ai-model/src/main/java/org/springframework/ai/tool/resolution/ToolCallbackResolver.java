package org.springframework.ai.tool.resolution;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;

public interface ToolCallbackResolver {

	@Nullable
	ToolCallback resolve(String toolName);

}
