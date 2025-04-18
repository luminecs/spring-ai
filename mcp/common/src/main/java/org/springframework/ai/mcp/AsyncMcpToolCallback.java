package org.springframework.ai.mcp;

import java.util.Map;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

public class AsyncMcpToolCallback implements ToolCallback {

	private final McpAsyncClient asyncMcpClient;

	private final Tool tool;

	public AsyncMcpToolCallback(McpAsyncClient mcpClient, Tool tool) {
		this.asyncMcpClient = mcpClient;
		this.tool = tool;
	}

	@Override
	public ToolDefinition getToolDefinition() {
		return ToolDefinition.builder()
			.name(McpToolUtils.prefixedToolName(this.asyncMcpClient.getClientInfo().name(), this.tool.name()))
			.description(this.tool.description())
			.inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
			.build();
	}

	@Override
	public String call(String functionInput) {
		Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);

		return this.asyncMcpClient.callTool(new CallToolRequest(this.tool.name(), arguments)).map(response -> {
			if (response.isError() != null && response.isError()) {
				throw new IllegalStateException("Error calling tool: " + response.content());
			}
			return ModelOptionsUtils.toJsonString(response.content());
		}).block();
	}

	@Override
	public String call(String toolArguments, ToolContext toolContext) {

		return this.call(toolArguments);
	}

}
