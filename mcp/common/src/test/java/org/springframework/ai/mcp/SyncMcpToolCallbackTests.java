package org.springframework.ai.mcp;

import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.model.ToolContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncMcpToolCallbackTests {

	@Mock
	private McpSyncClient mcpClient;

	@Mock
	private Tool tool;

	@Test
	void getToolDefinitionShouldReturnCorrectDefinition() {

		var clientInfo = new Implementation("testClient", "1.0.0");
		when(this.mcpClient.getClientInfo()).thenReturn(clientInfo);
		when(this.tool.name()).thenReturn("testTool");
		when(this.tool.description()).thenReturn("Test tool description");

		SyncMcpToolCallback callback = new SyncMcpToolCallback(this.mcpClient, this.tool);

		var toolDefinition = callback.getToolDefinition();

		assertThat(toolDefinition.name()).isEqualTo(clientInfo.name() + "_testTool");
		assertThat(toolDefinition.description()).isEqualTo("Test tool description");
	}

	@Test
	void callShouldHandleJsonInputAndOutput() {

		when(this.tool.name()).thenReturn("testTool");
		CallToolResult callResult = mock(CallToolResult.class);
		when(this.mcpClient.callTool(any(CallToolRequest.class))).thenReturn(callResult);

		SyncMcpToolCallback callback = new SyncMcpToolCallback(this.mcpClient, this.tool);

		String response = callback.call("{\"param\":\"value\"}");

		assertThat(response).isNotNull();
	}

	@Test
	void callShoulIngroeToolContext() {

		when(this.tool.name()).thenReturn("testTool");
		CallToolResult callResult = mock(CallToolResult.class);
		when(this.mcpClient.callTool(any(CallToolRequest.class))).thenReturn(callResult);

		SyncMcpToolCallback callback = new SyncMcpToolCallback(this.mcpClient, this.tool);

		String response = callback.call("{\"param\":\"value\"}", new ToolContext(Map.of("foo", "bar")));

		assertThat(response).isNotNull();
	}

}
