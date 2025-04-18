package org.springframework.ai.tool.execution;

@FunctionalInterface
public interface ToolExecutionExceptionProcessor {

	String process(ToolExecutionException exception);

}
