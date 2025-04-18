package org.springframework.ai.tool.definition;

import java.lang.reflect.Method;

import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.util.Assert;

public interface ToolDefinition {

	String name();

	String description();

	String inputSchema();

	static DefaultToolDefinition.Builder builder() {
		return DefaultToolDefinition.builder();
	}

	static DefaultToolDefinition.Builder builder(Method method) {
		Assert.notNull(method, "method cannot be null");
		return DefaultToolDefinition.builder()
			.name(ToolUtils.getToolName(method))
			.description(ToolUtils.getToolDescription(method))
			.inputSchema(JsonSchemaGenerator.generateForMethodInput(method));
	}

	static ToolDefinition from(Method method) {
		return ToolDefinition.builder(method).build();
	}

}
