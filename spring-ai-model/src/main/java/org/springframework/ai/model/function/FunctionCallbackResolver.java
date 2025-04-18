package org.springframework.ai.model.function;

import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.lang.NonNull;

@Deprecated
public interface FunctionCallbackResolver {

	FunctionCallback resolve(@NonNull String name);

}
