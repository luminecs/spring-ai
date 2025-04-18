package org.springframework.ai.mcp.client.autoconfigure;

import io.modelcontextprotocol.spec.McpClientTransport;

public record NamedClientMcpTransport(String name, McpClientTransport transport) {

}
