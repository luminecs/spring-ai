package org.springframework.ai.tool.execution;

import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ToolCallResultConverter {

	String convert(@Nullable Object result, @Nullable Type returnType);

}
