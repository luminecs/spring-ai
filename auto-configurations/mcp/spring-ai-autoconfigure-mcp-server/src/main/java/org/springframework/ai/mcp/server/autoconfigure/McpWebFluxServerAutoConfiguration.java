package org.springframework.ai.mcp.server.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.reactive.function.server.RouterFunction;

@AutoConfiguration
@ConditionalOnClass({ WebFluxSseServerTransportProvider.class })
@ConditionalOnMissingBean(McpServerTransportProvider.class)
@Conditional(McpServerStdioDisabledCondition.class)
public class McpWebFluxServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WebFluxSseServerTransportProvider webFluxTransport(ObjectProvider<ObjectMapper> objectMapperProvider,
			McpServerProperties serverProperties) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
		return new WebFluxSseServerTransportProvider(objectMapper, serverProperties.getSseMessageEndpoint(),
				serverProperties.getSseEndpoint());
	}

	@Bean
	public RouterFunction<?> webfluxMcpRouterFunction(WebFluxSseServerTransportProvider webFluxProvider) {
		return webFluxProvider.getRouterFunction();
	}

}
