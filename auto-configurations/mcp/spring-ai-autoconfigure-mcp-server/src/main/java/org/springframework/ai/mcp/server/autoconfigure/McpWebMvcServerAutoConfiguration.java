package org.springframework.ai.mcp.server.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@AutoConfiguration
@ConditionalOnClass({ WebMvcSseServerTransportProvider.class })
@ConditionalOnMissingBean(McpServerTransportProvider.class)
@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "stdio", havingValue = "false",
		matchIfMissing = true)
public class McpWebMvcServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider(
			ObjectProvider<ObjectMapper> objectMapperProvider, McpServerProperties serverProperties) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
		return new WebMvcSseServerTransportProvider(objectMapper, serverProperties.getSseMessageEndpoint());
	}

	@Bean
	public RouterFunction<ServerResponse> mvcMcpRouterFunction(WebMvcSseServerTransportProvider transportProvider) {
		return transportProvider.getRouterFunction();
	}

}
