package org.springframework.ai.tool.resolution;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;

public interface ToolCallbackResolver {

	@Nullable
	FunctionCallback resolve(String toolName);

}
