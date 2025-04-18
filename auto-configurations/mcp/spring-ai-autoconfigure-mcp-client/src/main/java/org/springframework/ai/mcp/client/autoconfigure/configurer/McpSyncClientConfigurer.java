package org.springframework.ai.mcp.client.autoconfigure.configurer;

import java.util.List;

import io.modelcontextprotocol.client.McpClient;

import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;

public class McpSyncClientConfigurer {

	private List<McpSyncClientCustomizer> customizers;

	public McpSyncClientConfigurer(List<McpSyncClientCustomizer> customizers) {
		this.customizers = customizers;
	}

	public McpClient.SyncSpec configure(String name, McpClient.SyncSpec spec) {
		applyCustomizers(name, spec);
		return spec;
	}

	private void applyCustomizers(String name, McpClient.SyncSpec spec) {
		if (this.customizers != null) {
			for (McpSyncClientCustomizer customizer : this.customizers) {
				customizer.customize(name, spec);
			}
		}
	}

}
