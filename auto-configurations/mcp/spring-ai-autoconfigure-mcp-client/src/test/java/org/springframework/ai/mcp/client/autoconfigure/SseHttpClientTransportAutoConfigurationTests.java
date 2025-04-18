package org.springframework.ai.mcp.client.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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

}
