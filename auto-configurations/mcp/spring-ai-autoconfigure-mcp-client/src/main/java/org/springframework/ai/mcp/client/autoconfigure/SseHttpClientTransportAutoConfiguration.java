package org.springframework.ai.mcp.client.autoconfigure;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SseWebFluxTransportAutoConfiguration.class)
@ConditionalOnClass({ McpSchema.class, McpSyncClient.class })
@ConditionalOnMissingClass("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport")
@EnableConfigurationProperties({ McpSseClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class SseHttpClientTransportAutoConfiguration {

	@Bean
	public List<NamedClientMcpTransport> mcpHttpClientTransports(McpSseClientProperties sseProperties,
			ObjectProvider<ObjectMapper> objectMapperProvider) {

		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

		List<NamedClientMcpTransport> sseTransports = new ArrayList<>();

		for (Map.Entry<String, SseParameters> serverParameters : sseProperties.getConnections().entrySet()) {

			var transport = new HttpClientSseClientTransport(HttpClient.newBuilder(), serverParameters.getValue().url(),
					objectMapper);
			sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
		}

		return sseTransports;
	}

}
