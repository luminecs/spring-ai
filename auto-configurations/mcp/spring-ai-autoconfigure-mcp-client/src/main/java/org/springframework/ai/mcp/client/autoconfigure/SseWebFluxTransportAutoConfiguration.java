package org.springframework.ai.mcp.client.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@ConditionalOnClass(WebFluxSseClientTransport.class)
@EnableConfigurationProperties({ McpSseClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class SseWebFluxTransportAutoConfiguration {

	@Bean
	public List<NamedClientMcpTransport> webFluxClientTransports(McpSseClientProperties sseProperties,
			ObjectProvider<WebClient.Builder> webClientBuilderProvider,
			ObjectProvider<ObjectMapper> objectMapperProvider) {

		List<NamedClientMcpTransport> sseTransports = new ArrayList<>();

		var webClientBuilderTemplate = webClientBuilderProvider.getIfAvailable(WebClient::builder);
		var objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

		for (Map.Entry<String, SseParameters> serverParameters : sseProperties.getConnections().entrySet()) {
			var webClientBuilder = webClientBuilderTemplate.clone().baseUrl(serverParameters.getValue().url());
			var transport = new WebFluxSseClientTransport(webClientBuilder, objectMapper);
			sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
		}

		return sseTransports;
	}

}
