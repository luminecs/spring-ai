package org.springframework.ai.mcp.client.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({ McpSchema.class })
@EnableConfigurationProperties({ McpStdioClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class StdioTransportAutoConfiguration {

	@Bean
	public List<NamedClientMcpTransport> stdioTransports(McpStdioClientProperties stdioProperties) {

		List<NamedClientMcpTransport> stdioTransports = new ArrayList<>();

		for (Map.Entry<String, ServerParameters> serverParameters : stdioProperties.toServerParameters().entrySet()) {
			var transport = new StdioClientTransport(serverParameters.getValue());
			stdioTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));

		}

		return stdioTransports;
	}

}
