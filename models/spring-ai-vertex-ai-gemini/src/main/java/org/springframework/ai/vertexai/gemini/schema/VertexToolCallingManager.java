package org.springframework.ai.vertexai.gemini.schema;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.util.Assert;

public class VertexToolCallingManager implements ToolCallingManager {

	private final ToolCallingManager delegateToolCallingManager;

	public VertexToolCallingManager(ToolCallingManager delegateToolCallingManager) {
		Assert.notNull(delegateToolCallingManager, "Delegate tool calling manager must not be null");
		this.delegateToolCallingManager = delegateToolCallingManager;
	}

	@Override
	public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {

		List<ToolDefinition> toolDefinitions = this.delegateToolCallingManager.resolveToolDefinitions(chatOptions);

		return toolDefinitions.stream().map(td -> {
			ObjectNode jsonSchema = JsonSchemaConverter.fromJson(td.inputSchema());
			ObjectNode openApiSchema = JsonSchemaConverter.convertToOpenApiSchema(jsonSchema);
			JsonSchemaGenerator.convertTypeValuesToUpperCase(openApiSchema);

			return ToolDefinition.builder()
				.name(td.name())
				.description(td.description())
				.inputSchema(openApiSchema.toPrettyString())
				.build();
		}).toList();
	}

	@Override
	public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
		return this.delegateToolCallingManager.executeToolCalls(prompt, chatResponse);
	}

}
