package org.springframework.ai.mcp;

import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

public class SyncMcpToolCallback implements ToolCallback {

	private final McpSyncClient mcpClient;

	private final Tool tool;

	public SyncMcpToolCallback(McpSyncClient mcpClient, Tool tool) {
		this.mcpClient = mcpClient;
		this.tool = tool;

	}

	@Override
	public ToolDefinition getToolDefinition() {
		return ToolDefinition.builder()
			.name(McpToolUtils.prefixedToolName(this.mcpClient.getClientInfo().name(), this.tool.name()))
			.description(this.tool.description())
			.inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
			.build();
	}

	@Override
	public String call(String functionInput) {
		Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);

		CallToolResult response = this.mcpClient.callTool(new CallToolRequest(this.tool.name(), arguments));
		if (response.isError() != null && response.isError()) {
			throw new IllegalStateException("Error calling tool: " + response.content());
		}
		return ModelOptionsUtils.toJsonString(response.content());
	}

	@Override
	public String call(String toolArguments, ToolContext toolContext) {

		return this.call(toolArguments);
	}

}
