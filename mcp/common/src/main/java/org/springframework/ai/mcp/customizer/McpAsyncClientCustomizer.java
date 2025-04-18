package org.springframework.ai.mcp.customizer;

import io.modelcontextprotocol.client.McpClient;

public interface McpAsyncClientCustomizer {

	void customize(String name, McpClient.AsyncSpec spec);

}
