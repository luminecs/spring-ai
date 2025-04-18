package org.springframework.ai.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class SyncMcpToolCallbackProvider implements ToolCallbackProvider {

	private final List<McpSyncClient> mcpClients;

	private final BiPredicate<McpSyncClient, Tool> toolFilter;

	public SyncMcpToolCallbackProvider(BiPredicate<McpSyncClient, Tool> toolFilter, List<McpSyncClient> mcpClients) {
		Assert.notNull(mcpClients, "MCP clients must not be null");
		Assert.notNull(toolFilter, "Tool filter must not be null");
		this.mcpClients = mcpClients;
		this.toolFilter = toolFilter;
	}

	public SyncMcpToolCallbackProvider(List<McpSyncClient> mcpClients) {
		this((mcpClient, tool) -> true, mcpClients);
	}

	public SyncMcpToolCallbackProvider(BiPredicate<McpSyncClient, Tool> toolFilter, McpSyncClient... mcpClients) {
		this(toolFilter, List.of(mcpClients));
	}

	public SyncMcpToolCallbackProvider(McpSyncClient... mcpClients) {
		this(List.of(mcpClients));
	}

	@Override
	public ToolCallback[] getToolCallbacks() {

		var toolCallbacks = new ArrayList<>();

		this.mcpClients.stream()
			.forEach(mcpClient -> toolCallbacks.addAll(mcpClient.listTools()
				.tools()
				.stream()
				.filter(tool -> this.toolFilter.test(mcpClient, tool))
				.map(tool -> new SyncMcpToolCallback(mcpClient, tool))
				.toList()));
		var array = toolCallbacks.toArray(new ToolCallback[0]);
		validateToolCallbacks(array);
		return array;
	}

	private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException(
					"Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
		}
	}

	public static List<ToolCallback> syncToolCallbacks(List<McpSyncClient> mcpClients) {

		if (CollectionUtils.isEmpty(mcpClients)) {
			return List.of();
		}
		return List.of((new SyncMcpToolCallbackProvider(mcpClients).getToolCallbacks()));
	}

}
