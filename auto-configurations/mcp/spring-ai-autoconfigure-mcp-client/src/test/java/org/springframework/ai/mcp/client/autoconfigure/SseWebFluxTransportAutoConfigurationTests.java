package org.springframework.ai.mcp.client.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class SseWebFluxTransportAutoConfigurationTests {

	private final ApplicationContextRunner applicationContext = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(SseWebFluxTransportAutoConfiguration.class));

	@Test
	void webFluxClientTransportsPresentIfWebFluxSseClientTransportPresent() {

		this.applicationContext.run(context -> assertThat(context.containsBean("webFluxClientTransports")).isTrue());
	}

	@Test
	void webFluxClientTransportsNotPresentIfMissingWebFluxSseClientTransportNotPresent() {

		this.applicationContext
			.withClassLoader(
					new FilteredClassLoader("io.modelcontextprotocol.client.transport.WebFluxSseClientTransport"))
			.run(context -> assertThat(context.containsBean("webFluxClientTransports")).isFalse());
	}

	@Test
	void webFluxClientTransportsNotPresentIfMcpClientDisabled() {

		this.applicationContext.withPropertyValues("spring.ai.mcp.client.enabled", "false")
			.run(context -> assertThat(context.containsBean("webFluxClientTransports")).isFalse());
	}

}
