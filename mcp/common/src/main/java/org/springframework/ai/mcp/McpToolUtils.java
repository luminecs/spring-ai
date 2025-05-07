package org.springframework.ai.mcp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micrometer.common.util.StringUtils;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Role;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;

public final class McpToolUtils {

	public static final String TOOL_CONTEXT_MCP_EXCHANGE_KEY = "exchange";

	private McpToolUtils() {
	}

	public static String prefixedToolName(String prefix, String toolName) {

		if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(toolName)) {
			throw new IllegalArgumentException("Prefix or toolName cannot be null or empty");
		}

		String input = prefix + "_" + toolName;

		String formatted = input.replaceAll("[^a-zA-Z0-9_-]", "");

		formatted = formatted.replaceAll("-", "_");

		if (formatted.length() > 64) {
			formatted = formatted.substring(formatted.length() - 64);
		}

		return formatted;
	}

	public static List<McpServerFeatures.SyncToolSpecification> toSyncToolSpecification(
			List<ToolCallback> toolCallbacks) {
		return toolCallbacks.stream().map(McpToolUtils::toSyncToolSpecification).toList();
	}

	public static List<McpServerFeatures.SyncToolSpecification> toSyncToolSpecifications(
			ToolCallback... toolCallbacks) {
		return toSyncToolSpecification(List.of(toolCallbacks));
	}

	public static McpServerFeatures.SyncToolSpecification toSyncToolSpecification(ToolCallback toolCallback) {
		return toSyncToolSpecification(toolCallback, null);
	}

	public static McpServerFeatures.SyncToolSpecification toSyncToolSpecification(ToolCallback toolCallback,
			MimeType mimeType) {

		var tool = new McpSchema.Tool(toolCallback.getToolDefinition().name(),
				toolCallback.getToolDefinition().description(), toolCallback.getToolDefinition().inputSchema());

		return new McpServerFeatures.SyncToolSpecification(tool, (exchange, request) -> {
			try {
				String callResult = toolCallback.call(ModelOptionsUtils.toJsonString(request),
						new ToolContext(Map.of(TOOL_CONTEXT_MCP_EXCHANGE_KEY, exchange)));
				if (mimeType != null && mimeType.toString().startsWith("image")) {
					return new McpSchema.CallToolResult(List
						.of(new McpSchema.ImageContent(List.of(Role.ASSISTANT), null, callResult, mimeType.toString())),
							false);
				}
				return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(callResult)), false);
			}
			catch (Exception e) {
				return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(e.getMessage())), true);
			}
		});
	}

	public static Optional<McpSyncServerExchange> getMcpExchange(ToolContext toolContext) {
		if (toolContext != null && toolContext.getContext().containsKey(TOOL_CONTEXT_MCP_EXCHANGE_KEY)) {
			return Optional
				.ofNullable((McpSyncServerExchange) toolContext.getContext().get(TOOL_CONTEXT_MCP_EXCHANGE_KEY));
		}
		return Optional.empty();
	}

	public static List<McpServerFeatures.AsyncToolSpecification> toAsyncToolSpecifications(
			List<ToolCallback> toolCallbacks) {
		return toolCallbacks.stream().map(McpToolUtils::toAsyncToolSpecification).toList();
	}

	public static List<McpServerFeatures.AsyncToolSpecification> toAsyncToolSpecifications(
			ToolCallback... toolCallbacks) {
		return toAsyncToolSpecifications(List.of(toolCallbacks));
	}

	public static McpServerFeatures.AsyncToolSpecification toAsyncToolSpecification(ToolCallback toolCallback) {
		return toAsyncToolSpecification(toolCallback, null);
	}

	public static McpServerFeatures.AsyncToolSpecification toAsyncToolSpecification(ToolCallback toolCallback,
			MimeType mimeType) {

		McpServerFeatures.SyncToolSpecification syncToolSpecification = toSyncToolSpecification(toolCallback, mimeType);

		return new AsyncToolSpecification(syncToolSpecification.tool(),
				(exchange, map) -> Mono
					.fromCallable(() -> syncToolSpecification.call().apply(new McpSyncServerExchange(exchange), map))
					.subscribeOn(Schedulers.boundedElastic()));
	}

	public static List<ToolCallback> getToolCallbacksFromSyncClients(McpSyncClient... mcpClients) {
		return getToolCallbacksFromSyncClients(List.of(mcpClients));
	}

	public static List<ToolCallback> getToolCallbacksFromSyncClients(List<McpSyncClient> mcpClients) {

		if (CollectionUtils.isEmpty(mcpClients)) {
			return List.of();
		}
		return List.of((new SyncMcpToolCallbackProvider(mcpClients).getToolCallbacks()));
	}

	public static List<ToolCallback> getToolCallbacksFromAsyncClients(McpAsyncClient... asyncMcpClients) {
		return getToolCallbacksFromAsyncClients(List.of(asyncMcpClients));
	}

	public static List<ToolCallback> getToolCallbacksFromAsyncClients(List<McpAsyncClient> asyncMcpClients) {

		if (CollectionUtils.isEmpty(asyncMcpClients)) {
			return List.of();
		}
		return List.of((new AsyncMcpToolCallbackProvider(asyncMcpClients).getToolCallbacks()));
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	// @formatter:off
	private record Base64Wrapper(@JsonAlias("mimetype") @Nullable MimeType mimeType, @JsonAlias({
			"base64", "b64", "imageData" }) @Nullable String data) {
	}

}
