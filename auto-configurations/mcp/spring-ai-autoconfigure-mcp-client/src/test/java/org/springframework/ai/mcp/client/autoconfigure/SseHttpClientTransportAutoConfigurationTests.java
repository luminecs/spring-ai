package org.springframework.ai.mcp.client.autoconfigure;

import java.lang.reflect.Field;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SseHttpClientTransportAutoConfigurationTests {

	private final ApplicationContextRunner applicationContext = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(SseHttpClientTransportAutoConfiguration.class));

	@Test
	void mcpHttpClientTransportsNotPresentIfMissingWebFluxSseClientTransportPresent() {
		this.applicationContext.run(context -> assertThat(context.containsBean("mcpHttpClientTransports")).isFalse());
	}

	@Test
	void mcpHttpClientTransportsPresentIfMissingWebFluxSseClientTransportNotPresent() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.run(context -> assertThat(context.containsBean("mcpHttpClientTransports")).isTrue());
	}

	@Test
	void mcpHttpClientTransportsNotPresentIfMcpClientDisabled() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.enabled", "false")
			.run(context -> assertThat(context.containsBean("mcpHttpClientTransports")).isFalse());
	}

	@Test
	void noTransportsCreatedWithEmptyConnections() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).isEmpty();
			});
	}

	@Test
	void singleConnectionCreatesOneTransport() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080")
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(1);
				assertThat(transports.get(0).name()).isEqualTo("server1");
				assertThat(transports.get(0).transport()).isInstanceOf(HttpClientSseClientTransport.class);
			});
	}

	@Test
	void multipleConnectionsCreateMultipleTransports() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081")
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(2);
				assertThat(transports).extracting("name").containsExactlyInAnyOrder("server1", "server2");
				assertThat(transports).extracting("transport")
					.allMatch(transport -> transport instanceof HttpClientSseClientTransport);
				for (NamedClientMcpTransport transport : transports) {
					assertThat(transport.transport()).isInstanceOf(HttpClientSseClientTransport.class);
					assertThat(getSseEndpoint((HttpClientSseClientTransport) transport.transport())).isEqualTo("/sse");
				}
			});
	}

	@Test
	void customSseEndpointIsRespected() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/custom-sse")
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(1);
				assertThat(transports.get(0).name()).isEqualTo("server1");
				assertThat(transports.get(0).transport()).isInstanceOf(HttpClientSseClientTransport.class);

				assertThat(getSseEndpoint((HttpClientSseClientTransport) transports.get(0).transport()))
					.isEqualTo("/custom-sse");
			});
	}

	@Test
	void customObjectMapperIsUsed() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withUserConfiguration(CustomObjectMapperConfiguration.class)
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080")
			.run(context -> {
				assertThat(context.getBean(ObjectMapper.class)).isNotNull();
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(1);
			});
	}

	@Test
	void defaultSseEndpointIsUsedWhenNotSpecified() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080")
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(1);
				assertThat(transports.get(0).name()).isEqualTo("server1");
				assertThat(transports.get(0).transport()).isInstanceOf(HttpClientSseClientTransport.class);

			});
	}

	@Test
	void mixedConnectionsWithAndWithoutCustomSseEndpoint() {
		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.withPropertyValues("spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080",
					"spring.ai.mcp.client.sse.connections.server1.sse-endpoint=/custom-sse",
					"spring.ai.mcp.client.sse.connections.server2.url=http://otherserver:8081")
			.run(context -> {
				List<NamedClientMcpTransport> transports = context.getBean("mcpHttpClientTransports", List.class);
				assertThat(transports).hasSize(2);
				assertThat(transports).extracting("name").containsExactlyInAnyOrder("server1", "server2");
				assertThat(transports).extracting("transport")
					.allMatch(transport -> transport instanceof HttpClientSseClientTransport);
				for (NamedClientMcpTransport transport : transports) {
					assertThat(transport.transport()).isInstanceOf(HttpClientSseClientTransport.class);
					if (transport.name().equals("server1")) {
						assertThat(getSseEndpoint((HttpClientSseClientTransport) transport.transport()))
							.isEqualTo("/custom-sse");
					}
					else {
						assertThat(getSseEndpoint((HttpClientSseClientTransport) transport.transport()))
							.isEqualTo("/sse");
					}
				}
			});
	}

	private String getSseEndpoint(HttpClientSseClientTransport transport) {
		Field privateField = ReflectionUtils.findField(HttpClientSseClientTransport.class, "sseEndpoint");
		ReflectionUtils.makeAccessible(privateField);
		return (String) ReflectionUtils.getField(privateField, transport);
	}

	@Configuration
	static class CustomObjectMapperConfiguration {

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

	}

}
