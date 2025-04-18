package org.springframework.ai.mcp.server.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

@AutoConfiguration
@ConditionalOnClass({ WebFluxSseServerTransportProvider.class })
@ConditionalOnMissingBean(McpServerTransportProvider.class)
@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "stdio", havingValue = "false",
		matchIfMissing = true)
public class McpWebFluxServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WebFluxSseServerTransportProvider webFluxTransport(ObjectProvider<ObjectMapper> objectMapperProvider,
			McpServerProperties serverProperties) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
		return new WebFluxSseServerTransportProvider(objectMapper, serverProperties.getSseMessageEndpoint());
	}

	@Bean
	public RouterFunction<?> webfluxMcpRouterFunction(WebFluxSseServerTransportProvider webFluxProvider) {
		return webFluxProvider.getRouterFunction();
	}

}
