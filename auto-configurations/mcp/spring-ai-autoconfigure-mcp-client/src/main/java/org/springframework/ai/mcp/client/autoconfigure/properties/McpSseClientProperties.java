package org.springframework.ai.mcp.client.autoconfigure.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(McpSseClientProperties.CONFIG_PREFIX)
public class McpSseClientProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.client.sse";

	private final Map<String, SseParameters> connections = new HashMap<>();

	public Map<String, SseParameters> getConnections() {
		return this.connections;
	}

	public record SseParameters(String url) {
	}

}
