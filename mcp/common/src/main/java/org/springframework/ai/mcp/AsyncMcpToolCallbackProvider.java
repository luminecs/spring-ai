package org.springframework.ai.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Flux;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.util.CollectionUtils;

public class AsyncMcpToolCallbackProvider implements ToolCallbackProvider {

	private final List<McpAsyncClient> mcpClients;

	private final BiPredicate<McpAsyncClient, Tool> toolFilter;

	public AsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, Tool> toolFilter, List<McpAsyncClient> mcpClients) {
		Assert.notNull(mcpClients, "MCP clients must not be null");
		Assert.notNull(toolFilter, "Tool filter must not be null");
		this.mcpClients = mcpClients;
		this.toolFilter = toolFilter;
	}

	public AsyncMcpToolCallbackProvider(List<McpAsyncClient> mcpClients) {
		this((mcpClient, tool) -> true, mcpClients);
	}

	public AsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, Tool> toolFilter, McpAsyncClient... mcpClients) {
		this(toolFilter, List.of(mcpClients));
	}

	public AsyncMcpToolCallbackProvider(McpAsyncClient... mcpClients) {
		this(List.of(mcpClients));
	}

	@Override
	public ToolCallback[] getToolCallbacks() {

		List<ToolCallback> toolCallbackList = new ArrayList<>();

		for (McpAsyncClient mcpClient : this.mcpClients) {

			ToolCallback[] toolCallbacks = mcpClient.listTools()
				.map(response -> response.tools()
					.stream()
					.filter(tool -> this.toolFilter.test(mcpClient, tool))
					.map(tool -> new AsyncMcpToolCallback(mcpClient, tool))
					.toArray(ToolCallback[]::new))
				.block();

			validateToolCallbacks(toolCallbacks);

			toolCallbackList.addAll(List.of(toolCallbacks));
		}

		return toolCallbackList.toArray(new ToolCallback[0]);
	}

	private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException(
					"Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
		}
	}

	public static Flux<ToolCallback> asyncToolCallbacks(List<McpAsyncClient> mcpClients) {
		if (CollectionUtils.isEmpty(mcpClients)) {
			return Flux.empty();
		}

		return Flux.fromArray(new AsyncMcpToolCallbackProvider(mcpClients).getToolCallbacks());
	}

}
