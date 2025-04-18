package org.springframework.ai.mcp.customizer;

import io.modelcontextprotocol.client.McpClient;

public interface McpSyncClientCustomizer {

	void customize(String name, McpClient.SyncSpec spec);

}
