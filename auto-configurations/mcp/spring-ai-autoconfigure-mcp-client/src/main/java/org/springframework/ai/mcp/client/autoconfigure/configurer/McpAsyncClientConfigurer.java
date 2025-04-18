package org.springframework.ai.mcp.client.autoconfigure.configurer;

import java.util.List;

import io.modelcontextprotocol.client.McpClient;

import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;

public class McpAsyncClientConfigurer {

	private List<McpAsyncClientCustomizer> customizers;

	public McpAsyncClientConfigurer(List<McpAsyncClientCustomizer> customizers) {
		this.customizers = customizers;
	}

	public McpClient.AsyncSpec configure(String name, McpClient.AsyncSpec spec) {
		applyCustomizers(name, spec);
		return spec;
	}

	private void applyCustomizers(String name, McpClient.AsyncSpec spec) {
		if (this.customizers != null) {
			for (McpAsyncClientCustomizer customizer : this.customizers) {
				customizer.customize(name, spec);
			}
		}
	}

}
